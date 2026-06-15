package homework2;

public class DomainMessage {

    private final Command command;
    private final int userId;
    private final String payload; // string with params

    public DomainMessage(Command command, int userId, String payload) {
        this.command = command;
        this.userId = userId;
        this.payload = payload;
    }

    public Command getCommand()  { return command; }
    public int getUserId()       { return userId; }
    public String getPayload()   { return payload; }

    @Override
    public String toString() {
        return "DomainMessage{command = " + command + ", userId = " + userId + ", payload = '" + payload + "'}";
    }
}