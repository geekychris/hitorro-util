/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core;

import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.log.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CPU Information utilities.
 * Provides cross-platform methods to retrieve CPU details such as clock speed.
 *
 * @author chris
 */
public class CPUInfo {

	public static void main(String[] args) {
		ClockSpeed speed = getCPUClockSpeed();
		if (speed != null) {
			System.out.println("CPU Clock Speed: " + speed);
		} else {
			System.out.println("Unable to determine CPU clock speed");
		}
	}

	/**
	 * Represents CPU clock speed information
	 */
	public static class ClockSpeed {
		private final double speedMHz;
		private final double speedGHz;
		private final String source;
		
		public ClockSpeed(double speedMHz, String source) {
			this.speedMHz = speedMHz;
			this.speedGHz = speedMHz / 1000.0;
			this.source = source;
		}
		
		public double getSpeedMHz() {
			return speedMHz;
		}
		
		public double getSpeedGHz() {
			return speedGHz;
		}
		
		public String getSource() {
			return source;
		}
		
		@Override
		public String toString() {
			return String.format("%.2f GHz (%.0f MHz) [%s]", speedGHz, speedMHz, source);
		}
	}
	
	/**
	 * Get the CPU clock speed in MHz.
	 * This method works on both Linux and Mac OS X.
	 *
	 * @return ClockSpeed object containing speed information, or null if unable to determine
	 */
	@CommandDef(command = "cpu.clockspeed", description = "Get CPU clock speed")
	public static ClockSpeed getCPUClockSpeed() {
		Platform platform = Platform.getPlatform();
		
		if (platform == Platform.Linux) {
			return getLinuxCPUClockSpeed();
		} else if (platform == Platform.MacOSX) {
			return getMacCPUClockSpeed();
		}
		
		Log.util.warn("CPU clock speed detection not supported for platform: %s", platform.getShortName());
		return null;
	}
	
	/**
	 * Get CPU clock speed on Linux systems.
	 * Reads from /proc/cpuinfo and parses the "cpu MHz" field.
	 *
	 * @return ClockSpeed object or null if unable to determine
	 */
	private static ClockSpeed getLinuxCPUClockSpeed() {
		File cpuInfo = new File("/proc/cpuinfo");
		if (!cpuInfo.exists()) {
			Log.util.warn("Cannot read /proc/cpuinfo");
			return null;
		}
		
		try (BufferedReader reader = new BufferedReader(new FileReader(cpuInfo))) {
			Pattern pattern = Pattern.compile("cpu\\s+MHz\\s*:\\s*([0-9.]+)");
			String line;
			List<Double> speeds = new ArrayList<>();
			
			while ((line = reader.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					double mhz = Double.parseDouble(matcher.group(1));
					speeds.add(mhz);
				}
			}
			
			if (!speeds.isEmpty()) {
				// Return average speed across all cores
				double avgSpeed = speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
				return new ClockSpeed(avgSpeed, "/proc/cpuinfo");
			}
		} catch (Exception e) {
			Log.util.error("Error reading CPU clock speed from /proc/cpuinfo: %s %e", e, e);
		}
		
		return null;
	}
	
	/**
	 * Get CPU clock speed on Mac OS X systems.
	 * Uses the sysctl command to query hw.cpufrequency.
	 * Note: On Apple Silicon Macs, frequency information may not be available
	 * as the CPU uses dynamic frequency scaling with different performance cores.
	 *
	 * @return ClockSpeed object or null if unable to determine
	 */
	private static ClockSpeed getMacCPUClockSpeed() {
		// Try hw.cpufrequency first (returns Hz) - works on Intel Macs
		ClockSpeed speed = executeSysctl("hw.cpufrequency", 1_000_000.0);
		if (speed != null) {
			return speed;
		}
		
		// Fallback to hw.cpufrequency_max - works on some Intel Macs
		speed = executeSysctl("hw.cpufrequency_max", 1_000_000.0);
		if (speed != null) {
			return speed;
		}
		
		// Try sysctl machdep.cpu.brand_string and parse frequency from there
		speed = parseCPUBrandString();
		if (speed != null) {
			return speed;
		}
		
		// On Apple Silicon, check if we can get chip info at least
		String chipInfo = getAppleSiliconChipInfo();
		if (chipInfo != null) {
			Log.util.info("Apple Silicon detected: %s (dynamic frequency scaling - no fixed clock speed)", chipInfo);
		} else {
			Log.util.warn("Unable to determine CPU clock speed on Mac");
		}
		return null;
	}
	
	/**
	 * Get Apple Silicon chip information if available.
	 *
	 * @return Chip name or null
	 */
	private static String getAppleSiliconChipInfo() {
		try {
			Process process = Runtime.getRuntime().exec(new String[]{"sysctl", "-n", "machdep.cpu.brand_string"});
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line = reader.readLine();
				if (line != null && line.trim().startsWith("Apple")) {
					return line.trim();
				}
			}
			process.waitFor();
		} catch (Exception e) {
			// Silently continue
		}
		return null;
	}
	
	/**
	 * Execute a sysctl command and parse the result.
	 *
	 * @param key the sysctl key to query
	 * @param divisor divisor to convert result to MHz
	 * @return ClockSpeed object or null if command fails
	 */
	private static ClockSpeed executeSysctl(String key, double divisor) {
		try {
			Process process = Runtime.getRuntime().exec(new String[]{"sysctl", "-n", key});
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line = reader.readLine();
				if (line != null && !line.trim().isEmpty()) {
					double hz = Double.parseDouble(line.trim());
					double mhz = hz / divisor;
					return new ClockSpeed(mhz, "sysctl " + key);
				}
			}
			process.waitFor();
		} catch (Exception e) {
			// Silently continue to next method
		}
		return null;
	}
	
	/**
	 * Parse CPU brand string from sysctl to extract clock speed.
	 * Example: "Intel(R) Core(TM) i7-9750H CPU @ 2.60GHz"
	 *
	 * @return ClockSpeed object or null if unable to parse
	 */
	private static ClockSpeed parseCPUBrandString() {
		try {
			Process process = Runtime.getRuntime().exec(new String[]{"sysctl", "-n", "machdep.cpu.brand_string"});
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line = reader.readLine();
				if (line != null) {
					// Look for pattern like "@ 2.60GHz" or "@ 2600MHz"
					Pattern ghzPattern = Pattern.compile("@\\s*([0-9.]+)\\s*GHz", Pattern.CASE_INSENSITIVE);
					Pattern mhzPattern = Pattern.compile("@\\s*([0-9.]+)\\s*MHz", Pattern.CASE_INSENSITIVE);
					
					Matcher ghzMatcher = ghzPattern.matcher(line);
					if (ghzMatcher.find()) {
						double ghz = Double.parseDouble(ghzMatcher.group(1));
						return new ClockSpeed(ghz * 1000.0, "sysctl machdep.cpu.brand_string");
					}
					
					Matcher mhzMatcher = mhzPattern.matcher(line);
					if (mhzMatcher.find()) {
						double mhz = Double.parseDouble(mhzMatcher.group(1));
						return new ClockSpeed(mhz, "sysctl machdep.cpu.brand_string");
					}
				}
			}
			process.waitFor();
		} catch (Exception e) {
			Log.util.error("Error parsing CPU brand string: %s %e", e, e);
		}
		return null;
	}
	
	/**
	 * Get the CPU clock speed in MHz as a double.
	 * Convenience method that returns just the numeric value.
	 *
	 * @return CPU clock speed in MHz, or -1.0 if unable to determine
	 */
	@CommandDef(command = "cpu.clockspeed.mhz", description = "Get CPU clock speed in MHz")
	public static double getCPUClockSpeedMHz() {
		ClockSpeed speed = getCPUClockSpeed();
		return speed != null ? speed.getSpeedMHz() : -1.0;
	}
	
	/**
	 * Get the CPU clock speed in GHz as a double.
	 * Convenience method that returns just the numeric value.
	 *
	 * @return CPU clock speed in GHz, or -1.0 if unable to determine
	 */
	@CommandDef(command = "cpu.clockspeed.ghz", description = "Get CPU clock speed in GHz")
	public static double getCPUClockSpeedGHz() {
		ClockSpeed speed = getCPUClockSpeed();
		return speed != null ? speed.getSpeedGHz() : -1.0;
	}
	
	/**
	 * Get a formatted string describing the CPU clock speed.
	 *
	 * @return Formatted string with CPU clock speed, or "Unknown" if unable to determine
	 */
	@CommandDef(command = "cpu.clockspeed.formatted", description = "Get formatted CPU clock speed string")
	public static String getCPUClockSpeedFormatted() {
		ClockSpeed speed = getCPUClockSpeed();
		return speed != null ? speed.toString() : "Unknown";
	}
}
