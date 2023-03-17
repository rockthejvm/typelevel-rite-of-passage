package com.rockthejvm.jobsboard.core

import cats.*
import cats.implicits.*
import org.typelevel.log4cats.Logger

import com.stripe.{Stripe => TheStripe}
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams

import scala.util.Try
import scala.jdk.OptionConverters.*

import com.rockthejvm.jobsboard.logging.syntax.*
import com.rockthejvm.jobsboard.config.*
import com.stripe.net.Webhook

trait Stripe[F[_]] {
  def createCheckoutSession(jobId: String, userEmail: String): F[Option[Session]]
  def handleWebhookEvent[A](
      payload: String,
      signature: String,
      action: String => F[A]
  ): F[Option[A]]
}

class LiveStripe[F[_]: MonadThrow: Logger](
    key: String,
    price: String,
    successUrl: String,
    cancelUrl: String,
    webhookSecret: String
) extends Stripe[F] {
  // globally set constant
  TheStripe.apiKey = key

  override def createCheckoutSession(jobId: String, userEmail: String): F[Option[Session]] =
    SessionCreateParams
      .builder()
      .setMode(SessionCreateParams.Mode.PAYMENT)
      .setInvoiceCreation(
        SessionCreateParams.InvoiceCreation
          .builder()
          .setEnabled(true)
          .build()
      )
      .setPaymentIntentData(
        SessionCreateParams.PaymentIntentData
          .builder()
          .setReceiptEmail(userEmail)
          .build()
      )
      .setSuccessUrl(s"$successUrl/$jobId")
      .setCancelUrl(cancelUrl)
      .setCustomerEmail(userEmail)
      .setClientReferenceId(jobId) // will be sent back to me by the webhook
      .addLineItem(
        SessionCreateParams.LineItem
          .builder()
          .setQuantity(1L)
          .setPrice(price)
          .build()
      )
      .build()
      .pure[F]
      .map(params => Session.create(params))
      .map(_.some)
      .logError(error => s"Creating checkout session failed: $error")
      .recover { case _ => None }

  override def handleWebhookEvent[A](
      payload: String,
      signature: String,
      action: String => F[A]
  ): F[Option[A]] =
    MonadThrow[F]
      .fromTry(
        Try(
          Webhook.constructEvent(
            payload,
            signature,
            webhookSecret
          )
        )
      )
      .logError(e => "Stripe security verification failed - possibly faking attempt")
      .flatMap { event =>
        event.getType() match {
          case "checkout.session.completed" =>
            event
              .getDataObjectDeserializer()
              .getObject()                   // Optional[deserialuzer]
              .toScala                       // Option[deserialuzer]
              .map(_.asInstanceOf[Session])  // Option[Session]
              .map(_.getClientReferenceId()) // Option[String] <-- stores my job id
              .map(action)                   // Option[F[A]] // performing the effect
              .sequence                      // F[Option[A]]
              .log(
                {
                  case None =>
                    s"Event ${event.getId()} not producing any effect - check Stripe dashboard"
                  case Some(v) => s"Event ${event.getId()} fully paid - OK"
                },
                e => s"Webhook action failed: $e"
              )

          case _ =>
            // discard the effect
            None.pure[F]
        }
      }
      .logError(e => s"Something else went wrong: $e")
      .recover { case _ => None }

}

object LiveStripe {
  def apply[F[_]: MonadThrow: Logger](stripeConfig: StripeConfig): F[LiveStripe[F]] =
    new LiveStripe[F](
      stripeConfig.key,
      stripeConfig.price,
      stripeConfig.successUrl,
      stripeConfig.cancelUrl,
      stripeConfig.webhookSecret
    ).pure[F]
}
