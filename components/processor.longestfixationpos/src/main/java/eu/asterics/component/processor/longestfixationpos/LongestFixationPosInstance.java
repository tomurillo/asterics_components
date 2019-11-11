

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

package eu.asterics.component.processor.longestfixationpos;


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
 * Returns the eye gaze position of the longest fixation as given by a Tobii eye tracking device.
 * To be used in conjunction with the EyeX AsTeRICS plug-in (https://www.asterics.eu/plugins/sensors/EyeX.html)
 *
 * @author Tomas Murillo-Morales [Tomas.Murillo_Morales@jku.at]
 *         Date: 11-11-2019
 */
public class LongestFixationPosInstance extends AbstractRuntimeComponentInstance
{
	// Input ports
	final IRuntimeOutputPort opGazeXout = new DefaultRuntimeOutputPort();
	final IRuntimeOutputPort opGazeYout = new DefaultRuntimeOutputPort();

	// Plug-in properties
	boolean propIgnoreFixation = false;

	// Member variables
	protected int longestFixation = -1;
	protected int longestX = -1;
	protected int longestY = -1;
	protected boolean collectGaze = false;

   /**
    * The class constructor.
    */
    public LongestFixationPosInstance()
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
		if ("fixationTime".equalsIgnoreCase(portID))
		{
			return ipFixationTime;
		}
		if ("gazeXin".equalsIgnoreCase(portID))
		{
			return ipGazeXin;
		}
		if ("gazeYin".equalsIgnoreCase(portID))
		{
			return ipGazeYin;
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
		if ("gazeXout".equalsIgnoreCase(portID))
		{
			return opGazeXout;
		}
		if ("gazeYout".equalsIgnoreCase(portID))
		{
			return opGazeYout;
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
		if ("sendAndReset".equalsIgnoreCase(eventPortID))
		{
			return elpSendAndReset;
		}

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
		if ("ignoreFixation".equalsIgnoreCase(propertyName))
		{
			return propIgnoreFixation;
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
		if ("ignoreFixation".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propIgnoreFixation;
			if("true".equalsIgnoreCase((String)newValue))
			{
				propIgnoreFixation = true;
			}
			else if("false".equalsIgnoreCase((String)newValue))
			{
				propIgnoreFixation = false;
			}
			return oldValue;
		}

        return null;
    }

     /**
      * Input Ports for receiving values.
      */
	private final IRuntimeInputPort ipFixationTime  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final int in = ConversionUtils.intFromBytes(data);
			if (in > longestFixation) {
				collectGaze = true;
				longestFixation = in;
			} else {
				collectGaze = false;
			}
		}
	};

	private final IRuntimeInputPort ipGazeXin  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			if (collectGaze || propIgnoreFixation) {
				final int in = ConversionUtils.intFromBytes(data);
				longestX = in;
			}
		}
	};

	private final IRuntimeInputPort ipGazeYin  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			if (collectGaze || propIgnoreFixation) {
				final int in = ConversionUtils.intFromBytes(data);
				longestY = in;
			}
		}
	};


     /**
      * Event Listerner Ports.
      */
	final IRuntimeEventListenerPort elpSendAndReset = new IRuntimeEventListenerPort()
	{
		public void receiveEvent(final String data)
		{
			if (allReady()) {
				opGazeXout.sendData(ConversionUtils.intToBytes(longestX));
				opGazeYout.sendData(ConversionUtils.intToBytes(longestY));
			}
			resetFlags();
		}
	};

	protected boolean allReady() {
		return longestX >= 0 && longestY >= 0 && longestFixation > 0;
	}

	protected void resetFlags() {
		longestX = -1;
		longestY = -1;
		longestFixation = -1;
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
		  this.resetFlags();
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
		  this.resetFlags();
          super.stop();
      }
}