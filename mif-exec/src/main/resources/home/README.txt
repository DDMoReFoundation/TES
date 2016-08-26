To run it:
1. open command line window
2. navigate to the directory containing this file
3. type in: startup.bat
Or:
1. navigate to the directory containing this file in Windows Explorer
2. double-click on the startup.bat file

You shouldn't need to update any of the default properties. For details on integration and custom encryption key see 'Notes' section below.

Notes:
1. since this is a local MIF, everything is executed as a tomcat user
2. You don't need to provide a password in the execution request (but you still need to provide a user name)
3. Set submitAsUser executionRequest attribute as 'false'
4. if you change an encryption key (so it is different than the one provided in this binary), you will need to specify:
mif.serviceAccount.userName=
mif.serviceAccount.userPassword=
in mif/etc/mif.properties file.

To encrypt the password you will need to use the MIFClient-binary.zip.

To kill the service, submit a POST request to: '/cs/stop' endpoint.

Connectors are loaded from the MIF_CONNECTORS_LOCATION variable set in the startup.bat.

Logging
service log is in mif/log/mif.log