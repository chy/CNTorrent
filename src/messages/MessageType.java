package messages;

public enum MessageType
{

	CHOKE			(0),
	UNCHOKE			(1),
	INTERESTED		(2),
	NOT_INTERESTED	(3),
	HAVE			(4),
	BITFIELD		(5),
	REQUEST			(6),
	PIECE			(7);

	private int value;

	private MessageType(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}

	public static MessageType getMessageType(int value)
	{
		for (MessageType messageType : MessageType.values())
		{
			if (messageType.getValue() == value)
				return messageType;
		}
		throw new RuntimeException(value + " is not a valid message type value");
	}

}
