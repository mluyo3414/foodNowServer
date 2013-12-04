foodNowServer
=============

James Dagres, 
Miguel Suarez,  
Matt Luckam, and
Carl Barbee.

DESCRIPTION:
=============

This is the server side for the food now application. This application is designed to send orders from an android device to this server for any restaurant. There is also an admin/employee application that can manipulate the orders accordingly with this server. We use Jetty and Amazon EC2. This server uses a database to store all of the orders in case the server crashes.
When an order is received from the client application it generates a unique random order confirmation number and sends it back to the client. The information received from the client is the order contents, the order name, the order total, the payment method and a phone number. Furthermore, when an order is marked as made from the admin application the server uses Twilio to text the phone number listed under the order efficiently notifying the customer when their order is ready.


CLIENT APPLICATION:
=============

For more information see the repo of the client:
https://github.com/mluyo3414/clientApp

ADMIN APPLICATION:
=============

For more information see the repo of the Admin App:
https://github.com/mluyo3414/adminApp


