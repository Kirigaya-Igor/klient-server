import java.io.Serializable;

class Message implements Serializable
{
        private String message;
        private byte[] file;
        private boolean isValidate, nickAccepted;
        private String recipient, sender;

        private Message(MessageBuilder msgbd)
        {
                this.message = msgbd.message;
                this.file = msgbd.file;
                this.recipient = msgbd.recipient;
                this.sender = msgbd.sender;
                this.isValidate = msgbd.isValidate;
                this.nickAccepted = msgbd.nickAccepted;

        }

        String getMessage()
        {
                return message;
        }

        byte[] getFile()
        {
                return file;
        }

        String getRecipient()
        {
                return recipient;
        }

        boolean getIsValidate()
        {
                return isValidate;
        }

        boolean getNickAccepted()
        {
                return nickAccepted;
        }

        String getSender()
        {
                return sender;
        }

        static class MessageBuilder
        {
                private String message;
                private byte[] file;
                private boolean isValidate, nickAccepted;
                private String recipient, sender;

                MessageBuilder message(String message)
                {
                        this.message = message;
                        return this;
                }

                MessageBuilder file(byte... file)
                {
                        this.file = file;
                        return this;
                }

                MessageBuilder validate()
                {
                        this.isValidate = true;
                        return this;
                }

                MessageBuilder recipient(String nick)
                {
                        this.recipient = nick;
                        return this;
                }

                MessageBuilder sender(String nick)
                {
                        this.sender = nick;
                        return this;
                }

                MessageBuilder acceptNick()
                {
                        this.nickAccepted = true;
                        return this;
                }

                Message build()
                {
                        return new Message( this );
                }
        }
}