# openshift-ldapsync
A small OpenShift LDAP Group Synctool in situations where the oadm group sync just is not good enough.


## When to use
As long as you are fine with the configurable oadm group sync you should use that. There is no reason for using this 
small additional program at all.

## Usage
The software may be run within a pod. It will load configuration data for the external services from the environment.
Currently the server data and credentials of the LDAP server and the OpenShift API endpoint and the token is read from
the default POD data. So the pod needs to run with an service account with permission to sync the data.


### Installation

#### Step 1: Set up account with sufficient permissions

We need a nice and warm project to create our pod in. You may choose any project you like but you need to be able to run
a pod in it. If you don't already have a management project for such cases, you may create one. This document assumes 
that the project is named '**cloudmgmt**' (all examples will take that project name, if your project is named otherwise,
you will have to change a few names like the ones of the service account used). In case of doubt, create it:

```shell
oc new-project cloudmgmt
```

or change into it:

```shell
oc project cloudmgmt
```

And in **cloudmgmt** we create the service account **system:serviceaccount:cloudmgmt:ldap-sync**:

```shell
oc create sa ldap-sync
```

And next we have to create the role containing all permissions we need (basically anything that can be done to groups).

`oc create -f `[`ldapsync.yaml`](doc/ldapsync-role.yaml)

The user created needs to be assigned the new role.

```shell
oc adm policy add-cluster-role-to-user ldapsync system:serviceaccount:cloudmgmt:ldap-sync 
```

As last step, the token of the user must be 

### Create The LDAP Credentials

Create the LDAP credentials as basic-auth secret:

```shell
oc secret new-basicauth ldap-sync-ldapcredentials --username=<bindDN> --password=<bindPassword>
```

or if you don't want to have the password on the command line:

```shell
oc secret new-basicauth ldap-sync-ldapcredentials --username=<bindDN> --prompt=true
```

### Create the new application

#### Changes to the file [ldapsync-app.yaml](docs/ldapsync-app.yaml)

There are some configurations needed in this file to be able to create a working configuration:

The LDAP Configuration needs to have the baseDN and the hostname of the LDAP server for group searches. And we need to 
have the correct link to the OpenShift cluster API. All three values are inserted into the pod as environment variables.

Please insert them in lines 81, 83 and 85 according to the description there.

#### Source secrets
If you need a source secret to retrieve the source, just add the two lines 

```yaml
    sourceSecret:
      name: <the name of your secret>
```
after line 27 (before the triggers) to the file `ldapsync-app.yaml`. The easiest way to create that secret is on the
OpenShift console on "Resources/Secret -> Create Secret" (select "Source Secret" as type).

#### Create the application
After these changes the build- and deployment config can be created.

`oc create -f `[`adsync-bc.yaml`](docs/ldapsync-bc.yaml)

