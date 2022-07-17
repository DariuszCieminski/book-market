# Book Market

The purpose of this application is to catalog all your books in one place and create a handy library.\
If you want to do away books you don't need, you can mark them as to be sold, set the price you want to sell them for,
and wait for other people to make an offer to buy them.\
After you accept someone's offer, you can agree with each other how the transaction will be made, by using private
messaging system.\
A legacy version of the application is available [HERE](https://bookmarket-pl.herokuapp.com/).

The application was made using Spring Boot framework and provides following functionality:

1. Adding books, changing their for sale status and price
2. Viewing other users' books for sale
3. Private messaging system
4. User authentication by JWT tokens
5. Validation of incoming data

### Running the application

#### Prerequisite - Apache Maven tool must be installed

Before running the application, the application.yml file has to be modified by providing data of utilized database and
email server.

After this is set, the application can be started by using Maven command:\
```mvn spring-boot:run```