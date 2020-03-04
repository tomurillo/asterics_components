

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
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.lang.Math;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.*;
import java.util.LinkedList;
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
 * Collect and combine heterogeneous data sources into a single JSON string output.
 *
 * @author Tomas Murillo-Morales [Tomas.Murillo_Morales@jku.at]
 *         Date: 26/09/2019
 */
public class DataCollectorInstance extends AbstractRuntimeComponentInstance
{

	public static final int DEFAULT_BATCH_SIZE = 5;
	public static final int DEFAULT_DELAY = 1000;  // Default delay of 1 second

	final IRuntimeOutputPort opOut = new DefaultRuntimeOutputPort();

	final IRuntimeEventTriggererPort etpdataCollected = new DefaultRuntimeEventTriggererPort();

	int propActivePorts = 1;
	String propKey1 = "key1";
	String operator1 = "operator1";
	String propKey2 = "key2";
	String operator2 = "operator2";
	String propKey3 = "key3";
	String operator3 = "operator3";
	String propKey4 = "key4";
	String operator4 = "operator4";
	String propKey5 = "key5";
	String operator5 = "operator5";
	int sampleDelay = DEFAULT_DELAY;
	int batchSize = DEFAULT_BATCH_SIZE;
	boolean waitForAll = false;

	// Member variables
	double ipIn1collector = 0;  // Collects latest received value from port 1
	boolean ipIn1ready = false;  // True if value received from port 1 can be sent out (is meaningful)
	int count1 = 0;  // Number of received discrete values from port 1
	double ipIn2collector = 0;
	boolean ipIn2ready = false;
	int count2 = 0;
	double ipIn3collector = 0;
	boolean ipIn3ready = false;
	int count3 = 0;
	double ipIn4collector = 0;
	boolean ipIn4ready = false;
	int count4 = 0;
	double ipIn5collector = 0;
	boolean ipIn5ready = false;
	int count5 = 0;
	boolean allReady = false;  // Everything is ready for one sample to be generated
	long startTime = 0L;
	double longestFixation = -1.0;
	int latestX = -1;
	int latestY = -1;
	int longestX = -1;
	int longestY = -1;
	HashMap<Integer, Double> maxVals = new HashMap<Integer, Double>();  // Max. values received on each port
	HashMap<Integer, Double> minVals = new HashMap<Integer, Double>();  // Min. values received on each port
    Map<Integer, Collection<Double>> sequences = new HashMap<Integer, Collection<Double>>();  // Sequences of a sample
	LinkedList<String> samples = new LinkedList<String>();  // Queue of samples to be sent out in a single batch

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
		if ("gazeX".equalsIgnoreCase(portID))
		{
			return ipGazeX;
		}
		if ("gazeY".equalsIgnoreCase(portID))
		{
			return ipGazeY;
		}
		if ("fixation".equalsIgnoreCase(portID))
		{
			return ipFixation;
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
		if ("dataCollected".equalsIgnoreCase(eventPortID)) {
			return etpdataCollected;
		}
        return null;
    }
		
    /**
     * returns the value of the given property.
     * @param propertyName   the name of the property
     * @return               the property value or null if not found
     */
    public Object getRuntimePropertyValue(String propertyName)
    {
		if ("sampleDelay".equalsIgnoreCase(propertyName)) {
			return sampleDelay;
		}
		else if ("batchSize".equalsIgnoreCase(propertyName))
		{
			return batchSize;
		}
		else if ("activePorts".equalsIgnoreCase(propertyName))
		{
			return propActivePorts;
		}
		else if ("waitForAll".equalsIgnoreCase(propertyName))
		{
			return waitForAll;
		}
		else if ("key1".equalsIgnoreCase(propertyName))
		{
			return propKey1;
		}
		else if ("operator1".equalsIgnoreCase(propertyName))
		{
			return operator1;
		}
		else if ("key2".equalsIgnoreCase(propertyName))
		{
			return propKey2;
		}
		else if ("operator2".equalsIgnoreCase(propertyName))
		{
			return operator2;
		}
		else if ("key3".equalsIgnoreCase(propertyName))
		{
			return propKey3;
		}
		else if ("operator3".equalsIgnoreCase(propertyName))
		{
			return operator3;
		}
		else if ("key4".equalsIgnoreCase(propertyName))
		{
			return propKey4;
		}
		else if ("operator4".equalsIgnoreCase(propertyName))
		{
			return operator4;
		}
		else if ("key5".equalsIgnoreCase(propertyName))
		{
			return propKey5;
		}
		else if ("operator5".equalsIgnoreCase(propertyName))
		{
			return operator5;
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
		if ("sampleDelay".equalsIgnoreCase(propertyName)) {
			final Integer oldValue = sampleDelay;
			sampleDelay = Integer.parseInt(newValue.toString());
			return oldValue;
		}
		else if ("batchSize".equalsIgnoreCase(propertyName))
		{
			final Integer oldValue = batchSize;
			batchSize = Integer.parseInt(newValue.toString());
			return oldValue;
		}
		else if ("activePorts".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propActivePorts;
			propActivePorts = Integer.parseInt(newValue.toString());
			return oldValue;
		}
		else if ("waitForAll".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = waitForAll;
			waitForAll = Boolean.parseBoolean((String) newValue);
			return oldValue;
		}
		else if ("key1".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propKey1;
			propKey1 = (String)newValue;
			return oldValue;
		}
		else if ("operator1".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = operator1;
			operator1 = (String)newValue;
			return oldValue;
		}
		else if ("key2".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propKey2;
			propKey2 = (String)newValue;
			return oldValue;
		}
		else if ("operator2".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = operator2;
			operator2 = (String)newValue;
			return oldValue;
		}
		else if ("key3".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propKey3;
			propKey3 = (String)newValue;
			return oldValue;
		}
		else if ("operator3".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = operator3;
			operator3 = (String)newValue;
			return oldValue;
		}
		else if ("key4".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propKey4;
			propKey4 = (String)newValue;
			return oldValue;
		}
		else if ("operator4".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = operator4;
			operator4 = (String)newValue;
			return oldValue;
		}
		else if ("key5".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = propKey5;
			propKey5 = (String)newValue;
			return oldValue;
		}
		else if ("operator5".equalsIgnoreCase(propertyName))
		{
			final Object oldValue = operator5;
			operator5 = (String)newValue;
			return oldValue;
		}
        return null;
    }

    protected void checkForFixation(String propKey, double value) {
		if (propKey != null && value >= longestFixation) {
			longestFixation = value;
		}
	}

     /**
      * Input Ports for receiving values.
      */
	private final IRuntimeInputPort ipIn1  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final double in = ConversionUtils.doubleFromBytes(data);
			if (in > 0) {
				ipIn1collector = applyOperator(ipIn1collector, ++count1, in, operator1, 1);
				ipIn1ready = true;
				createSampleIfReady();
			}
		}
	};
	private final IRuntimeInputPort ipIn2  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final double in = ConversionUtils.doubleFromBytes(data);
			if (in > 0) {
				ipIn2collector = applyOperator(ipIn2collector, ++count2, in, operator2, 2);
				ipIn2ready = true;
				createSampleIfReady();
			}
		}
	};
	private final IRuntimeInputPort ipIn3  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final double in = ConversionUtils.doubleFromBytes(data);
			if (in > 0) {
				ipIn3collector = applyOperator(ipIn3collector, ++count3, in, operator3, 3);
				ipIn3ready = true;
				createSampleIfReady();
			}
		}
	};
	private final IRuntimeInputPort ipIn4  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final double in = ConversionUtils.doubleFromBytes(data);
			if (in > 0) {
				ipIn4collector = applyOperator(ipIn4collector, ++count4, in, operator4, 4);
				ipIn4ready = true;
				createSampleIfReady();
			}
		}
	};
	private final IRuntimeInputPort ipIn5  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final double in = ConversionUtils.doubleFromBytes(data);
			if (in > 0) {
				ipIn5collector = applyOperator(ipIn5collector, ++count5, in, operator5, 5);
				ipIn5ready = true;
				createSampleIfReady();
			}
		}
	};
	private final IRuntimeInputPort ipGazeX  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final int in = ConversionUtils.intFromBytes(data);
			latestX = in;
		}
	};
	private final IRuntimeInputPort ipGazeY  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final int in = ConversionUtils.intFromBytes(data);
			latestY = in;
		}
	};

	private final IRuntimeInputPort ipFixation  = new DefaultRuntimeInputPort()
	{
		public void receiveData(byte[] data)
		{
			final int in = ConversionUtils.intFromBytes(data);
			if (in > 0 && in >= longestFixation) {
				longestFixation = in;
				longestX = latestX;
				longestY = latestY;
			}
		}
	};

	/**
	 * Utility methods
	 */


	/**
	 * Creates a new sample if enough values have been received through the input ports. Also outputs a batch when all
	 * samples have been created.
	 */
	private void createSampleIfReady() {
		if (allReady()) {
			startTime = System.currentTimeMillis();
			String sample = combineInputs(startTime);
			samples.add(sample);
			resetBuffers();
			if (samples.size() >= batchSize) {
				sendBatch();
			}
		}
	}

	private void sendBatch() {
		opOut.sendData(ConversionUtils.stringToBytes(combineSamples()));
		etpdataCollected.raiseEvent();
		samples.clear();
	}

	/**
	 * Returns whether all input ports are ready i.e. every port has already received at least one value
	 * and whether enough time has passed for one sample to be collected
	 * @return
	 */
	private boolean allReady() {
		boolean ready = false;
		if (waitForAll) {
			if (!allReady && propActivePorts > 0) {
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
			}
		} else {
			if (propActivePorts > 0) {
				ready = ipIn1ready;
				if (!ready && propActivePorts > 1) {
					ready = ipIn2ready;
				}
				if (!ready && propActivePorts > 2) {
					ready = ipIn3ready;
				}
				if (!ready && propActivePorts > 3) {
					ready = ipIn4ready;
				}
				if (!ready && propActivePorts > 4) {
					ready = ipIn5ready;
				}
			}
		}
		allReady = ready;
		if (allReady && this.sampleDelay > 0) {
			long endTime = System.currentTimeMillis();
			if (endTime - this.startTime < this.sampleDelay) {
				allReady = false;
			}
		}
		return allReady;
	}

	/**
	 * Apply the given operator to the data received from a port
	 * @return
	 */
	private double applyOperator(double in_prev, int count, double in, String op, int portNo) {
		double retVal = 0.0;
		if ("average".equalsIgnoreCase(op) || "avg".equalsIgnoreCase(op)) {
			retVal = in_prev + ((in - in_prev) / count);  // Incremental average
		} else if ("sum".equalsIgnoreCase(op)) {
			retVal = in_prev + in;
		} else if (op == null || op.isEmpty() || "none".equalsIgnoreCase(op) || "no".equalsIgnoreCase(op)) {
			retVal = in;
		} else if ("max".equalsIgnoreCase(op)) {
			double max_prev = (double) (maxVals.containsKey(portNo) ? maxVals.get(portNo) : 0.0);
			if (in > max_prev) {
				retVal = in;
				maxVals.put(portNo, in);
			} else {
				retVal = max_prev;
			}
		} else if ("min".equalsIgnoreCase(op)) {
			double min_prev = (double) (minVals.containsKey(portNo) ? minVals.get(portNo) : 0.0);
			if (in < in_prev) {
				retVal = in;
				minVals.put(portNo, in);
			} else {
				retVal = min_prev;
			}
		} else if ("mode".equalsIgnoreCase(op)) {
		} else {
			throw new IllegalArgumentException("Unknown operator!");
		}
        appendToSequence(in, portNo);
		return retVal;
	}

	private void appendToSequence(double in, int portNo) {
        Collection<Double> seq = sequences.get(portNo);
        if (seq == null) {
            seq = new LinkedList<Double>();
            sequences.put(portNo, seq);
        }
        seq.add(in);
    }

    private double getMode(int portNo) {
	    return getMode(portNo, 0.01);
    }

    private double getMode(int portNo, double tol) {
        double mode = 0.0;
        Collection<Double> seq = sequences.get(portNo);
        if (seq != null) {
            HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>();
            for (double s: seq) {
                int s_int = (int) (Math.round(s / tol));
                Integer count = countMap.get(s_int);
                countMap.put(s_int, count == null ? 1 : count + 1);
            }
            Entry<Integer, Integer> max = null;
            for (Entry<Integer, Integer> e: countMap.entrySet()) {
                if (max == null || e.getValue() > max.getValue()) {
                    max = e;
                }
            }
            mode = ((double) max.getKey()) * tol;
        }
        return mode;
    }

    private double collectorOutput(int portNo) {
	    double out = 0.0;
	    double collector = 0.0;
	    String op = "";
        switch (portNo) {
            case 1:
                collector = ipIn1collector;
                op = operator1;
                break;
            case 2:
                collector = ipIn2collector;
                op = operator2;
                break;
            case 3:
                collector = ipIn3collector;
                op = operator3;
                break;
            case 4:
                collector = ipIn4collector;
                op = operator4;
                break;
            case 5:
                collector = ipIn5collector;
                op = operator5;
                break;
        }
        if ("mode".equalsIgnoreCase(op)) {
            out = getMode(portNo);
        } else {
            out = collector;
        }
        return out;
    }

	private void resetPortFlags() {
		this.resetPortFlags(false);
	}

	private void resetPortFlags(boolean resetBuffers) {
		allReady = false;
		ipIn1ready = false;
		ipIn2ready = false;
		ipIn3ready = false;
		ipIn4ready = false;
		ipIn5ready = false;
		if (resetBuffers) {
			resetBuffers();
		}
	}

	private void resetBuffers() {
		ipIn1collector = 0;
		count1 = 0;
		ipIn2collector = 0;
		count2 = 0;
		ipIn3collector = 0;
		count3 = 0;
		ipIn4collector = 0;
		count4 = 0;
		ipIn5collector = 0;
		count5 = 0;
		latestX = -1;
		latestY = -1;
		longestX = -1;
		longestY = -1;
		longestFixation = -1;
		maxVals.clear();
		minVals.clear();
		sequences.clear();
	}

	private String combineInputs() {
		return combineInputs(System.currentTimeMillis());
	}

	/**
	 * Returns the combined input port values as a JSON-formatted string
	 * @return String
	 */
	private String combineInputs(long timeStamp) {
		String retString = "";
		if (!allReady || propActivePorts <= 0) {
			return retString;
		}
		if (timeStamp > 0) {
			String fullTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Timestamp(timeStamp));
			retString = "{\"timestamp\":\"".concat(fullTimeStamp);
			retString = retString.concat("\",");
		} else {
			retString = "{";
		}
		retString = retString.concat("\"gazeX\":\"");
		retString = retString.concat(String.format("%d", longestX));
		retString = retString.concat("\",\"gazeY\":\"");
		retString = retString.concat(String.format("%d", longestY));
		retString = retString.concat("\",\"");
		retString = retString.concat(propKey1);
		retString = retString.concat("\":");
		if (!waitForAll && !ipIn1ready) {
			retString = retString.concat("\"no_data\"");
		} else {
			retString = retString.concat(String.format("%f", collectorOutput(1)));
		}
		if (propActivePorts > 1) {
			retString = retString.concat(",\"");
			retString = retString.concat(propKey2);
			retString = retString.concat("\":");
			if (!waitForAll && !ipIn2ready) {
				retString = retString.concat("\"no_data\"");
			} else {
				retString = retString.concat(String.format("%f", collectorOutput(2)));
			}
			if (propActivePorts > 2) {
				retString = retString.concat(",\"");
				retString = retString.concat(propKey3);
				retString = retString.concat("\":");
				if (!waitForAll && !ipIn3ready) {
					retString = retString.concat("\"no_data\"");
				} else {
					retString = retString.concat(String.format("%f", collectorOutput(3)));
				}
				if (propActivePorts > 3) {
					retString = retString.concat(",\"");
					retString = retString.concat(propKey4);
					retString = retString.concat("\":");
					if (!waitForAll && !ipIn4ready) {
						retString = retString.concat("\"no_data\"");
					} else {
						retString = retString.concat(String.format("%f", collectorOutput(4)));
					}
					if (propActivePorts > 4) {
						retString = retString.concat(",\"");
						retString = retString.concat(propKey5);
						retString = retString.concat("\":");
						if (!waitForAll && !ipIn5ready) {
							retString = retString.concat("\"no_data\"");
						} else {
							retString = retString.concat(String.format("%f", collectorOutput(5)));
						}
					}
				}
			}
		}
		retString = retString.concat("}");
		// AstericsErrorHandling.instance.getLogger().info("Output: " + retString);
		return retString;
	}

	private String combineSamples() {
		String retString = "{\"samples\":[";
		boolean first = true;
		for (String sample : this.samples) {
			if (!first) {
				retString = retString.concat(",");
			} else {
				first = false;
			}
			retString = retString.concat(sample);
		}
		retString = retString.concat("]}");
		return retString;
	}

     /**
      * called when model is started.
      */
      @Override
      public void start()
      {
      	  this.startTime = System.currentTimeMillis();
          super.start();
      }

     /**
      * called when model is paused.
      */
      @Override
      public void pause()
      {
		  this.resetPortFlags(true);
		  samples.clear();
          super.pause();
      }

     /**
      * called when model is resumed.
      */
      @Override
      public void resume()
      {
		  this.startTime = System.currentTimeMillis();
          super.resume();
      }

     /**
      * called when model is stopped.
      */
      @Override
      public void stop()
      {
		  this.resetPortFlags(true);
		  samples.clear();
          super.stop();
      }
}