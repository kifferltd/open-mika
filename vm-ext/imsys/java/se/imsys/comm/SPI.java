package se.imsys.comm;

/**
 * This class implements a SPI bus master interface. 
 * The bus uses pins PCE0-PCE3 for the SPI signals SCK (clock, PCE0), MOSI 
 * (master out/slave in, PCE1), MISO (master in/slave out, PCE2) and SS 
 * (slave select, PCE3). The maximum speed of the bus is around 1.4 Mbit/s. 
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 */
public class SPI {

	/**
	* The clock delay used if none specified. Results in a clock
    * speed of around 80 kHz.
	*/
	public static final int DEFAULT_BIT_RATE = 10;

	/**
	* The longest allowed clock delay. Results in a clock
    * speed of around 8 kHz.
	*/
	public static final int MAX_CLOCK_DELAY = 127;
	
		/**
	* The clock delay used if none specified. Results in a clock
    * speed of around 80 kHz.
	*/
	public static final int DEFAULT_CLOCK_DELAY = 10;


	private final int SPI_MODE_BITO	= 0x01;
	private final int SPI_MODE_CPOL	= 0x40;
	private final int SPI_MODE_CPHA	= 0x80;
	private final int SPI_MODE_PLSS	= 0x04;	
	private final int SPI_MODE_ENSS	= 0x02;

	// SPI config state						   
    private int port;
    private int bitMask;
	private int mode; 
	private int bitRateUser;
	private int bitRateActual;
	private int delay;

	private native void initSPI();
	private native int setPolarity0(int mode, boolean CPOL);
	private native int setPhase0(int mode, boolean CPHA);
	private native int setBitOrder0(int mode, boolean bitOrder);
	private native int setSlaveSelect0(int mode, int port, int bitMask, boolean use_ss, boolean pol_ss);
    private native int getBitRateActual(int bitRate);
	private native int getBitRate(int delay);
    private native int xmitSPI(int mode, byte[] ba, int off, int len, int bitRate);
	private native void setModeBit(int bit, boolean b);
    /**
     * Constructs an SPI object with default settings. These are:
     * <UL>
     *  <LI><code>port = 1</code> (Port B)
     *  <LI><code>bitMask = 1</code> (Bit 0)
     *  <LI><code>bitRate = DEFAULT_BIT_RATE</code>
     *  <LI><code>use_ss = true</code>
     *  <LI><code>pol_ss = true</code>
     *  <LI><code>CPOL = false</code>
     *  <LI><code>CPHA = false</code>
     *  <LI><code>bitOrder = false</code>
     *  </UL>
     */
	public SPI() {
		this(1, 1, DEFAULT_BIT_RATE, true, true, false, false, false);
	}

	
	
	/**
     * Constructs an SPI object with default settings. These are:
     * <UL>
     *  <LI><code>delay = DEFAULT_CLOCK_DELAY</code>
     *  <LI><code>use_ss = true</code>
     *  <LI><code>CPOL = false</code>
     *  <LI><code>CPHA = false</code>
     *  <LI><code>bitOrder = false</code>
     *  <LI><code>pol_ss = true</code>
     *  </UL>
     */
//	public SPI() {
//		this(DEFAULT_CLOCK_DELAY, false, true, false, false, false);
//	}
	
	
	
    /**
     * Constructs an SPI object with all settings specified. The exception
	 * is <code>pol_ss</code>, which has to be set using the
	 * <code>setSlaveSelectPolarity</code> method.
     *
	 * @param port Specifies which port to use, can be 0 for port A, 1 for port B etc.
     * @param bitMask Specifies which bit in the above port to use, can be 0x80 for bit 7, 0x40 for bit 6 etc.
     * @param freq Desired bitrate in kHz. This will be the maximum bitrate at any time.
     * @param use_ss set to <code>true</code> to enable use of the SS line
     * @param CPOL set to <code>true</code> to make SCK idle high
     * @param CPHA set to <code>true</code> to make first SCK edge active
     * @param bitOrder set to <code>true</code> to transmit lsb first
     */
    public SPI(int port, int bitMask, int freq, boolean use_ss, boolean pol_ss, boolean CPOL, boolean CPHA, boolean bitOrder) {

		mode = 0;

        setBitRate(freq);
	    setPhase(CPHA);
	    setBitOrder(bitOrder);
		mode = setSlaveSelect0(mode, port, bitMask, use_ss, pol_ss);
 		initSPI();
   }
   
	/**
     * Constructs an SPI object with all settings specified. The exception
	 * is <code>pol_ss</code>, which has to be set using the
	 * <code>setSlaveSelectPolarity</code> method.
     *
     * @param delay delay between clock edges
     * @param noskew no effect
     * @param use_ss set to <code>true</code> to enable use of the SS line
     * @param CPOL set to <code>true</code> to make SCK idle high
     * @param CPHA set to <code>true</code> to make first SCK edge active
     * @param bitOrder set to <code>true</code> to transmit lsb first
     */
    public SPI(int delay, boolean noskew, boolean use_ss, boolean CPOL, boolean CPHA, boolean bitOrder) {

        setClockDelay(delay);
 
		initSPI();
	    setModeBit(SPI_MODE_ENSS, use_ss);
	    setModeBit(SPI_MODE_PLSS, true);
	    setModeBit(SPI_MODE_CPOL, CPOL);
	    setModeBit(SPI_MODE_CPHA, CPHA);
	    setModeBit(SPI_MODE_BITO, bitOrder);
    }

   /**
     * Enables or disables use of the slave select (SS) line. If
     * set to <code>true</code>, the SS line is asserted before each
     * read or write operation and deasserted immediately following the
     * operation. Otherwise this line is unused.
     *
     * @param port Specifies which port to use, can be 0 for port A, 1 for port B etc.
     * @param bitMask Specifies which bit in the above port to use, can be 0x80 for bit 7, 0x40 for bit 6 etc.
     * @param use_ss set to <code>true</code> to enable use of SS line
     * @param pol_ss set to <code>true</code> to make SS line idle high
     */
	public void setSlaveSelect(int port, int bitMask, boolean use_ss, boolean pol_ss)
    {
		mode = setSlaveSelect0(mode, port, bitMask, use_ss, pol_ss);
	}
    /**
     * Selects the polarity of the slave select (SS) line. If set to <code>true</code>,
     * the SS line will idle high (active low), otherwise it will idle low (active high).
     *
     * @param pol_ss set to <code>true</code> to make SS line idle high
     */
    public void setSlaveSelectPolarity(boolean pol_ss) {
		if (pol_ss)
			mode &= ~SPI_MODE_PLSS;
   		else
 			mode |= SPI_MODE_PLSS;

	    setModeBit(SPI_MODE_PLSS, pol_ss);
    }
    /**
     * Enables or disables use of the slave select (SS) line. If
     * set to <code>true</code>, the SS line is asserted before each
     * read or write operation and deasserted immediately following the
     * operation. Otherwise this line is unused.
     *
     * @param use_ss set to <code>true</code> to enable use of SS line
     */
    public void enableSlaveSelect(boolean use_ss) {
		if (use_ss)
			mode |= SPI_MODE_ENSS;
	    else
			mode &= ~SPI_MODE_ENSS;

	    setModeBit(SPI_MODE_ENSS, use_ss);
    }

    /**
     * Sets SPI clock (SCK) polarity.  If set to <code>true</code>,
     * the SCK will idle high, otherwise the SCK will idle low.
     *
     * @param CPOL set to <code>true</code> to make SCK idle high
     */
	public void setPolarity(boolean CPOL) 
	{
		mode = setPolarity0(mode, CPOL);
	} 

    /**
     * Sets SPI clock (SCK) phase.  If set to <code>true</code>,
     * the first clock edge is active, otherwise the second clock
	 * edge is the first active edge.
     *
     * @param CPHA set to <code>true</code> to make first SCK edge active
     */
	public void setPhase(boolean CPHA) 
	{
		mode = setPhase0(mode, CPHA);
	} 

    /**
     * Sets SPI data bit order.
     * If set to <code>true</code>, the bit order will be least significant
     * bit (lsb) first, otherwise msb will be transmitted first.
     *
     * @param bitOrder set to <code>true</code> to transmit lsb first
     */
	public void setBitOrder(boolean bitOrder) 
	{
		mode = setBitOrder0(mode, bitOrder);
	} 


    /**
	 * Sends and receives data on the SPI interface. Sends <code>len</code> bytes
	 * of data on the MOSI line, and simultaneously receives the same amount of
	 * data on the MISO line. The received data replaces the sent data in the
	 * <code>ba</code> array, starting at <code>off</code>. All communcation
	 * settings must be set up in advance using the <code>set..</code> methods.
     *
     * @param ba array containing data to be sent and providing space for received data
     * @param off starting offset into <code>ba</code>
     * @param len number of bytes to send and receive
	 *
	 * @return 1 if successful, 0 if <code>len</code> parameter was zero.
     */
    public int xmit(byte[] ba, int off, int len) {
        // Bounds check array access (easier here than in native)
	    if ((len < 0) || (off < 0) || ((len + off) > ba.length))
    	   throw new ArrayIndexOutOfBoundsException();

        return xmitSPI(mode, ba, off, len, bitRateActual);
    }

    /**
	 * Sets SPI communication speed. Set the <code>delay</code> parameter to 0
	 * for the fastest communication, SCK cycle time will then be 700 ns.
	 * <code>delay</code> > 0 results in a SCK cycle time of approximately
	 * (2.8 + <code>delay</code>*1.0) µs.	 * 
     *
     * @param freq Desired bitrate in kHz. This will be the maximum bitrate at any time.
     * @throws IllegalArgumentException This exception will never be thrown.
     */
    public void setBitRate(int freq) throws IllegalArgumentException {
        bitRateUser = freq;
		bitRateActual = getBitRateActual(freq);
		
    }
	
	
	/**
	 * Sets SPI communication speed. Set the <code>delay</code> parameter to 0
	 * for the fastest communication, SCK cycle time will then be 700 ns.
	 * <code>delay</code> > 0 results in a SCK cycle time of approximately
	 * (2.8 + <code>delay</code>*1.0) µs.
     *
     * @param delay clock delay value between 0 and <code>MAX_CLOCK_DELAY</code>, inclusive
     * @throws IllegalArgumentException if <code>delay</code> is negative
     *     or greater than <code>MAX_CLOCK_DELAY</code>.
     */
    public void setClockDelay(int delay) throws IllegalArgumentException {
        if ((delay < 0) || (delay > MAX_CLOCK_DELAY)) {
            throw new IllegalArgumentException("Invalid clock delay:"+delay);
        }
        this.bitRateActual = getBitRate(delay);
    }
}
