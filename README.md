# CloudBees Bees Shop Demo

## Application Overview


<img alt="Bees Shop - CloudBees MySQL" src="https://raw.github.com/CloudBees-community/bees-shop-clickstart/master/src/site/img/bees-shop-screenshot.png" style="width: 70%;"/>

<img alt="Bees Shop - CloudBees MySQL" src="https://raw.github.com/CloudBees-community/bees-shop-clickstart/master/src/site/img/bees-shop-screenshot-2.png" style="width: 70%;"/>


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

### Externalizing environment specific configuration parameters

It is common to have environment specific parameters, parameters whose value is different in each environment such as webservices passwords or urls.

In the Bees Shop demo, the Amazon AWS parameters (`aws_access_key`, `aws_secret_key`, `aws_s3_bucket_name` and `aws_cloudfront_domain_name`) are environment specific.

A best practice to externalize such configuration parameter in Spring Applications is to use a `<context:property-placeholder />` that can by default load the configuration values for one environment (usually the dev environment) and then to override these default with values that resides outside of the application.

The external source to override the environments specific configuration can be a file or system properties.

On CloudBees, as you don't have access to the file system, we recommand to override default application parameters with System Properties.

The Spring configuration would look like

```xml
<beans ...>

    <context:property-placeholder system-properties-mode="OVERRIDE" location="classpath:bees-shop-dev-env.properties"/>

    <!-- AMAZON AWS -->

    <bean id="awsCredentials" class="com.amazonaws.auth.BasicAWSCredentials"
          c:accessKey="${aws_access_key}" c:secretKey="${aws_secret_key}"/>

    <bean id="amazonS3FileStorageService" class="com.cloudbees.demo.beesshop.service.AmazonS3FileStorageService"
          p:awsCredentials-ref="awsCredentials"
          p:amazonS3BucketName="${aws_s3_bucket_name}"
          p:amazonCloudFrontDomainName="${aws_cloudfront_domain_name}"
            />
</beans>
```

Where:
* `location="classpath:bees-shop-dev-env.properties"` targets a configuration file with values intended for dev apps (empty `aws_secret_key`, ... ) 
* `system-properties-mode="OVERRIDE" ` lets Spring framework overide values defined in `classpath:bees-shop-dev-env.properties` with system properties (e.g. `-D`values)
 
Once the application is able to override configuration parameters with externalized properties, you can inject value with CloudBees application parameters.

Sample

```sh
bees config:set -a MYAPP aws_access_key=123456
```

The combination of Spring Property Placeholder with CloudBees CLI is a best practice to manage environment specific configuration parameters on CloudBees platform. 

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

#### Spring Profile

```
bees config:update -a www src/main/config/cloudbees-config-bees-shop.xml
```

with `cloudbees-config-bees-shop.xml`

```xml
<!--
Run:

bees config:update -a www src/main/config/cloudbees-config-bees-shop-clc.xml
-->
<config>
    <!-- SPRING PROFILE-->
    <param name="spring.profiles.active" value="javaee"/>

    <!-- AMAZON AWS-->
    <!-- aws credentials -->
    <param name="aws_access_key" value="YOUR_ACCESS_KEY"/>
    <param name="aws_secret_key" value="YOUR_SECRET_KEY"/>
    <!-- amazon s3 -->
    <param name="aws_s3_bucket_name" value="bees-shop"/>
    <!-- amazon cloudfront-->
    <param name="aws_cloudfront_domain_name" value="YOUR_CLOUDFRONT_DOMAIN.cloudfront.net"/>

    <!-- MAIL -->
    <param name="mail_from" value="webmaster@beesshop.org"/>
</config>
```

