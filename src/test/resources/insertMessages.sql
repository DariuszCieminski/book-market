SET @FORMATTER = 'yyyy-MM-dd HH:mm:ssZ';

INSERT INTO MESSAGE (ID, IS_READ, SENT_ON, TEXT, RECEIVER_ID, SENDER_ID)
VALUES (1, true, PARSEDATETIME('2022-05-08 12:00:00+0100', @FORMATTER), 'Hello, how are you?', 2, 1);
INSERT INTO MESSAGE (ID, IS_READ, SENT_ON, TEXT, RECEIVER_ID, SENDER_ID)
VALUES (2, true, PARSEDATETIME('2022-05-09 09:30:00+0100', @FORMATTER), 'Thanks, I am fine :)', 1, 2);
INSERT INTO MESSAGE (ID, IS_READ, SENT_ON, TEXT, RECEIVER_ID, SENDER_ID)
VALUES (3, false, PARSEDATETIME('2022-06-14 18:43:26+0100', @FORMATTER), 'Please check my books for sale.', 2, 1);