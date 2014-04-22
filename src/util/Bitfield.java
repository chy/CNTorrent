package util;

import java.util.Arrays;

public class Bitfield
{

	private byte[] bitfield;

	public Bitfield(byte[] bitfield)
	{
		this.bitfield = bitfield;
	}

	public Bitfield(int numBits, boolean fillValue)
	{
		int tempLength = numBits / 8;
		int length = (tempLength * 8 == numBits) ? tempLength : (tempLength + 1);
		bitfield = new byte[length];
		byte fillValueByte = fillValue ? (byte) (0b11111111)
				: (byte) (0b00000000);
		Arrays.fill(bitfield, fillValueByte);
	}

	public byte[] getBitfield()
	{
		return bitfield;
	}

	public boolean hasPiece(int pieceIndex)
	{
		int index = pieceIndex / 8;
		int offset = pieceIndex % 8;
		int andResult = bitfield[index] & (1 << offset);
		return (andResult != 0);
	}

	public void setPiece(int pieceIndex, boolean havePiece)
	{
		int index = pieceIndex / 8;
		int offset = pieceIndex % 8;
		byte temp = (byte) (1 << offset);
		if (havePiece)
		{
			bitfield[index] |= temp;
		}
		else
		{
			temp = (byte) (~temp);
			bitfield[index] &= temp;
		}
	}

}
