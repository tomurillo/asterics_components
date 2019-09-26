

/*
 *    AsTeRICS - Assistive Technology Rapid Integration and Construction Set
 * 
 * 
 *        d8888      88888888888       8888888b.  8888888 .d8888b.   .d8888b. 
 *       d88888          888           888   Y88b   888  d88P  Y88b d88P  Y88b
 *      d88P888          888           888    888   888  888    888 Y88b.     
 *     d88P 888 .d8888b  888   .d88b.  888   d88P   888  888         "Y888b.  
 *    d88P  888 88K      888  d8P  Y8b 8888888P"    888  888            "Y88b.
 *   d88P   888 "Y8888b. 888  88888888 888 T88b     888  888    888       "888
 *  d8888888888      X88 888  Y8b.     888  T88b    888  Y88b  d88P Y88b  d88P
 * d88P     888  88888P' 888   "Y8888  888   T88b 8888888 "Y8888P"   "Y8888P" 
 *
 *
 *                    homepage: http://www.asterics.org 
 *
 *         This project has been funded by the European Commission, 
 *                      Grant Agreement Number 247730
 *  
 *  
 *         Dual License: MIT or GPL v3.0 with "CLASSPATH" exception
 *         (please refer to the folder LICENSE)
 * 
 */

package eu.asterics.component.processor.datacollector;


import java.util.logging.Logger;
import eu.asterics.mw.data.ConversionUtils;
import eu.asterics.mw.model.runtime.AbstractRuntimeComponentInstance;
import eu.asterics.mw.model.runtime.IRuntimeInputPort;
import eu.asterics.mw.model.runtime.IRuntimeOutputPort;
import eu.asterics.mw.model.runtime.IRuntimeEventListenerPort;
import eu.asterics.mw.model.runtime.IRuntimeEventTriggererPort;
import eu.asterics.mw.model.runtime.impl.DefaultRuntimeOutputPort;
import eu.asterics.mw.model.runtime.impl.DefaultRuntimeInputPort;
import eu.asterics.mw.model.runtime.impl.DefaultRuntimeEventTriggererPort;
import eu.asterics.mw.services.AstericsErrorHandling;
import eu.asterics.mw.services.AREServices;

/**
 * 
 * Collect and combine heterogeneous data sources into a single output.
 *
 * @author Tomas Murillo-Morales [Tomas.Murillo_Morales@jku.at]
 *         Date: 26/09/2019
 */
public class DataCollectorInstance extends AbstractRuntimeComponentInstance
{
	final IRuntimeOutputPort opOut = new DefaultRuntimeOutputPort();

	int propActivePorts = 1;
	String propKey1 = "key1";
	String propKey2 = "key2";
	String propKey3 = "key3";
	String propKey4 = "key4";
	String propKey5 = "key5";

	// Member variables
	double ipIn1collector = 0; // Collects latest received value from port 1
	boolean ipIn1ready = false; // True if value received from port 1 needs to be sent out
	double ipIn2collector = 0;
	boolean ipIn2ready = false;
	double ipIn3collector = 0;
	boolean ipIn3ready = false;
	double ipIn4collector = 0;
	boolean ipIn4ready = false;
	double ipIn5collector = 0;
	boolean ipIn5ready = false;
	boolean allReady = false; // All ports have already received at least one value; collectors have useful data

   /**
    * Class constructor.
    */
    public DataCollectorInstance()
    {
        // empty constructor
    }

   /**
    * returns an Input Port.
    * @param portID   the name of the port
    * @return         the input port or null if not found
    */
    public IRuntimeInputPort getInputPort(String portID)
    {
		if ("in1".equalsIgnoreCase(portID))
		{
			return ipIn1;
		}
		if ("in2".equalsIgnoreCase(portID))
		{
			return ipIn2;
		}
		if ("in3".equalsIgnoreCase(portID))
		{
			return ipIn3;
		}
		if ("in4".equalsIgnoreCase(portID))
		{
			return ipIn4;
		}
		if ("in5".equalsIgnoreCase(portID))
		{
			return ipIn5;
		}

		return null;
	}

    /**
     * returns an Output Port.
     * @param portID   the name of the port
     * @return         the output port or null if not found
     */
    public IRuntimeOutputPort getOutputPort(String portID)
	{
		if ("out".equalsIgnoreCase(portID))
		{
			return opOut;
		}

		return null;
	}

    /**
     * returns an Event Listener Port.
     * @param eventPortID   the name of the port
     * @return         the EventListener port or null if not found
     */
    public IRuntimeEventListenerPort getEventListenerPort(String eventPortID)
    {

        return null;
    }

    /**
     * returns an Event Triggerer Port.
     * @param eventPortID   the name of the port
     * @return         the EventTriggerer port or null if not found
     */
    public IRuntimeEventTriggererPort getEventTriggererPort(String eventPortID)
    {

        return null;
    }
		
    /**
     * returns the value of the given property.
     * @param propertyName   the name of the property
     * @return               the property value or null if not found
     */
    public Object getRuntimePropertyValue(String propertyName)
    {
		if ("activePorts".equalsIgnoreCase(propertyName))
		{
			return propActivePorts;
		}
		if ("key1".equalsIgnoreCase(propertyName))
		{
			return propKey1;
		}
		if ("key2".equalsIgnoreCase(propertyName))
		{
			return propKey2;
		}
		if ("key3".equalsIgnoreCase(propertyName))
		{
			return propKey3;
		}
		if ("key4".equalsIgnoreCase(propertyName))
		{
			return propKey4;
		}
		if ("key5".equalsIgnoreCase(propertyName))
		{
			return propKey5;
		}

        return null;
    }

    /**
     * sets a new value for the given property.
     * @param propertyName   the name of the property
     * @param newValue       the desired property value or null if not found
     */
    public Object setRuntimePropertyValue(String propertyName, Object newValue)
    {
		if ("activePorts".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propActivePorts;
			propActivePorts = Integer.parseInt(newValue.toString());
			return oldValue;
		}
		if ("key1".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propKey1;
			propKey1 = (String)newValue;
			return oldValue;
		}
		if ("key2".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propKey2;
			propKey2 = (String)newValue;
			return oldValue;
		}
		if ("key3".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propKey3;
			propKey3 = (String)newValue;
			return oldValue;
		}
		if ("key4".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propKey4;
			propKey4 = (String)newValue;
			return oldValue;
		}
		if ("key5".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propKey5;
			propKey5 = (String)newValue;
			return oldValue;
		}

        return null;
    }

     /**
      * Input Ports for receiving values.
      */
	private final IRuntimeInputPort ipIn1  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final double in = ConversionUtils.doubleFromBytes(data);
			ipIn1collector = in;
			ipIn1ready = true;
			if (allPortsReady()) {
				opOut.sendData(ConversionUtils.stringToBytes(combineInputs()));
			}
		}
	};
	private final IRuntimeInputPort ipIn2  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final double in = ConversionUtils.doubleFromBytes(data);
			ipIn2collector = in;
			ipIn2ready = true;
			if (allPortsReady()) {
				opOut.sendData(ConversionUtils.stringToBytes(combineInputs()));
			}
		}
	};
	private final IRuntimeInputPort ipIn3  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final double in = ConversionUtils.doubleFromBytes(data);
			ipIn3collector = in;
			ipIn3ready = true;
			if (allPortsReady()) {
				opOut.sendData(ConversionUtils.stringToBytes(combineInputs()));
			}
		}
	};
	private final IRuntimeInputPort ipIn4  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final double in = ConversionUtils.doubleFromBytes(data);
			ipIn4collector = in;
			ipIn4ready = true;
			if (allPortsReady()) {
				opOut.sendData(ConversionUtils.stringToBytes(combineInputs()));
			}
		}
	};
	private final IRuntimeInputPort ipIn5  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final double in = ConversionUtils.doubleFromBytes(data);
			ipIn5collector = in;
			ipIn5ready = true;
			if (allPortsReady()) {
				opOut.sendData(ConversionUtils.stringToBytes(combineInputs()));
			}
		}
	};

	/**
	 * Utility methods
	 */

	/**
	 * Returns whether all input ports are ready i.e. every port has already received at least one value
	 * @return
	 */
	private boolean allPortsReady() {
		if (!allReady && propActivePorts > 0) {
			boolean ready = false;
			ready = ipIn1ready;
			if (ready && propActivePorts > 1) {
				ready = ipIn2ready;
			}
			if (ready && propActivePorts > 2) {
				ready = ipIn3ready;
			}
			if (ready && propActivePorts > 3) {
				ready = ipIn4ready;
			}
			if (ready && propActivePorts > 4) {
				ready = ipIn5ready;
			}
			allReady = ready;
		}
		return allReady;
	}

	private void resetPortFlags() {
		allReady = false;
		ipIn1ready = false;
		ipIn2ready = false;
		ipIn3ready = false;
		ipIn4ready = false;
		ipIn5ready = false;
	}

	/**
	 * Returns the combined input port values as a JSON-formatted string
	 * @return String
	 */
	private String combineInputs() {
		String retString = "";
		if (!allReady || propActivePorts <= 0) {
			return retString;
		}
		retString = "{\"".concat(propKey1);
		retString = retString.concat("\":");
		retString = retString.concat(String.format("%f", ipIn1collector));
		if (propActivePorts > 1) {
			retString = retString.concat(",\"");
			retString = retString.concat(propKey2);
			retString = retString.concat("\":");
			retString = retString.concat(String.format("%f", ipIn2collector));
			if (propActivePorts > 2) {
				retString = retString.concat(",\"");
				retString = retString.concat(propKey3);
				retString = retString.concat("\":");
				retString = retString.concat(String.format("%f", ipIn3collector));
				if (propActivePorts > 3) {
					retString = retString.concat(",\"");
					retString = retString.concat(propKey4);
					retString = retString.concat("\":");
					retString = retString.concat(String.format("%f", ipIn4collector));
					if (propActivePorts > 4) {
						retString = retString.concat(",\"");
						retString = retString.concat(propKey5);
						retString = retString.concat("\":");
						retString = retString.concat(String.format("%f", ipIn5collector));
					}
				}
			}
		}
		retString = retString.concat("}");
		// AstericsErrorHandling.instance.getLogger().info("Output: " + retString);
		return retString;
	}
	

     /**
      * called when model is started.
      */
      @Override
      public void start()
      {
          super.start();
      }

     /**
      * called when model is paused.
      */
      @Override
      public void pause()
      {
		  this.resetPortFlags();
          super.pause();
      }

     /**
      * called when model is resumed.
      */
      @Override
      public void resume()
      {
          super.resume();
      }

     /**
      * called when model is stopped.
      */
      @Override
      public void stop()
      {
		  this.resetPortFlags();
          super.stop();
      }
}