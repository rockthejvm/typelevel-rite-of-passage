## Deploy to DigitalOcean

### Install and Run the Application as a Docker Container

Create a Droplet in DigitalOcean: https://www.digitalocean.com/community/tutorials/initial-server-setup-with-ubuntu-22-04

Install `docker` and `docker-compose` https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-22-04

Build the docker image of the project using the following command:

```shell
sbt docker:publishLocal
```

The execution creates a docker images called `typelevel-project-playground` with tag `1.0.0`. Change them in the `build.sbt` file if needed.

Verify the correctness of the above process with the following command, which should list the available docker images:

```shell
docker images
```

Export the docker image with the following command (you need `sudo` privileges to run `docker` commands) :

```shell
docker save -o typelevel-project-playground.tar typelevel-project-playground:1.0.0
```

Copy the `tar` file to the DigitalOcean droplet using `scp` or something equivalent.

In the droplet, load the docker image:

```shell
docker load -i typelevel-project-playground.tar
```

Copy to the droplet the `docker-compose.yml` contained in this project. Eventually, change the properties values to customize the execution of the application.

Run the containers in the droplet with the following command:

```shell
docker-compose up -d
```

Try to call an application endpoint to verify the app is up and running:

```shell
curl -v http://<public_ip>:4041/api/health
curl -v http://<public_ip>:4041/api/jobs/filters
```

### Secure the Application

Install Nginx in the droplet: https://www.digitalocean.com/community/tutorials/how-to-install-nginx-on-ubuntu-22-04

Under the folder `/etc/nginx/sites-available/` create a configuration file, let's say `jobs-dashboard.conf`, and copy the following text into it:

```
server {
  listen 80;
  server_name <SUBDOMAIN>.<DOMAIN>;
  location /api {
    proxy_pass http://localhost:8080;
  }
}
```

The above configuration configure Nginx as a reverse proxy, redirecting the traffic from port `<SUBDOMAIN>.<DOMAIN>:80` to the application listening on the internal URL `http://localhost:8080`

Configure the DNS of your domain provider (i.e. GoDaddy) to redirect the traffic for `<SUBDOMAIN>.<DOMAIN>` to the public IP address of the droplet.

Now, the application should be reachable from the internet with the following command:

```shell
curl -v http://<SUBDOMAIN>.<DOMAIN>/api/v1/companies
```

Finally, use Let's Encrypt to issue a new certificate and configure it inside the Nginx reverse proxy: https://www.digitalocean.com/community/tutorials/how-to-secure-nginx-with-let-s-encrypt-on-ubuntu-22-04. There no need to create a new configuration file. Certbot can retrieve the right one its own.

After the above configuration, the application should be reachable using SSL:

```shell
curl -v https://<SUBDOMAIN>.<DOMAIN>/api/v1/companies
```
