# CloudBees Bees Shop Demo

## Application Overview

## Architecture Overview

<img alt="Bees Shop - CloudBees MySQL" src="https://raw.github.com/CloudBees-community/bees-shop-clickstart/master/src/site/img/bees-shop-architecture.png" style="width: 70%;"/>


## Detailed Architecture

### Database with CloudBees My SQL

<img alt="Bees Shop - CloudBees MySQL" src="https://raw.github.com/CloudBees-community/bees-shop-clickstart/master/src/site/img/bees-shop-mysql.png" style="width: 50%;"/>

### Transactional emails with SendGrid

<img alt="Bees Shop - SendGrid" src="https://raw.github.com/CloudBees-community/bees-shop-clickstart/master/src/site/img/bees-shop-sendgrid.png" style="width: 50%;"/>

### Media library storage with Amazon S3

<img alt="Bees Shop - Amazon S3" src="https://raw.github.com/CloudBees-community/bees-shop-clickstart/master/src/site/img/bees-shop-s3-direct.png" style="width: 50%;"/>


### Media library optimisation with Amazon CloudFront CDN

<img alt="Bees Shop - Amazon CloudFront with Amazon S3" src="https://raw.github.com/CloudBees-community/bees-shop-clickstart/master/src/site/img/bees-shop-s3-and-cloudfront.png" style="width: 50%;"/>


# Create application manually

### Create Tomcat container

```sh
bees app:create -a beesshop -t tomcat7
```


### Create CloudBees MySQL Database

```sh
bees db:create beesshop-db
```

### Bind Tomcat container to database

```sh
bees app:bind -a beesshop -db beesshop-db -as beesshop
```

### Set application environment specific configuration

#### Spring Profile

```sh
bees config:set -a beesshop -P spring.profiles.active=javaee
```

#### Amazon AWS Configuration

```sh
bees config:set -a beesshop -P aws_access_key=YOUR_AWS_ACCESS_KEY
bees config:set -a beesshop -P aws_secret_key=YOUR_AWS_SECRET_KEY
bees config:set -a beesshop -P aws_s3_bucket_name=bees-shop
bees config:set -a beesshop -P aws_cloudfront_domain_name=YOUR_DOMAIN.cloudfront.net
```

#### Mail configuration

```sh
bees config:set -a beesshop -P mail_from=webmaster@beesshop.org
```



