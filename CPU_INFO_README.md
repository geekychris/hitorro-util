# CPU Information Utilities

## Overview

The `CPUInfo` class provides cross-platform utility functions to retrieve CPU information, specifically CPU clock speed, on Linux and Mac OS X systems.

## Features

- **Cross-platform support**: Works on Linux and Mac OS X
- **Multiple detection methods**: Uses platform-specific approaches for accurate results
- **Convenient return formats**: Get speed in MHz, GHz, or as a formatted string
- **Apple Silicon support**: Gracefully handles Apple Silicon Macs where fixed clock speeds are not available

## Usage

### Get CPU Clock Speed (Full Information)

```java
import com.hitorro.util.core.CPUInfo;
import com.hitorro.util.core.CPUInfo.ClockSpeed;

ClockSpeed speed = CPUInfo.getCPUClockSpeed();
if (speed != null) {
    System.out.println("CPU Speed: " + speed);
    System.out.println("MHz: " + speed.getSpeedMHz());
    System.out.println("GHz: " + speed.getSpeedGHz());
    System.out.println("Source: " + speed.getSource());
} else {
    System.out.println("Unable to determine CPU clock speed");
}
```

### Get CPU Clock Speed in MHz

```java
double mhz = CPUInfo.getCPUClockSpeedMHz();
if (mhz > 0) {
    System.out.println("CPU Speed: " + mhz + " MHz");
}
```

### Get CPU Clock Speed in GHz

```java
double ghz = CPUInfo.getCPUClockSpeedGHz();
if (ghz > 0) {
    System.out.println("CPU Speed: " + ghz + " GHz");
}
```

### Get Formatted String

```java
String formatted = CPUInfo.getCPUClockSpeedFormatted();
System.out.println("CPU Speed: " + formatted);
```

## Platform-Specific Behavior

### Linux

On Linux systems, the utility reads from `/proc/cpuinfo` and parses the "cpu MHz" field. If multiple cores report different speeds (e.g., due to dynamic frequency scaling), the average speed across all cores is returned.

Example `/proc/cpuinfo` entry:
```
cpu MHz         : 2600.000
```

### Mac OS X (Intel)

On Intel-based Macs, the utility uses `sysctl` to query hardware information:
1. First tries `hw.cpufrequency` (returns Hz)
2. Falls back to `hw.cpufrequency_max`
3. Falls back to parsing `machdep.cpu.brand_string` (e.g., "Intel(R) Core(TM) i7-9750H CPU @ 2.60GHz")

### Mac OS X (Apple Silicon)

On Apple Silicon Macs (M1, M2, M3, M4, etc.), fixed CPU clock speeds are not exposed through traditional sysctl interfaces because these chips use advanced dynamic frequency scaling with performance and efficiency cores running at different speeds.

The utility will:
- Attempt all detection methods
- Log the chip information (e.g., "Apple M4 Max")
- Return `null` as there is no single fixed clock speed

This is expected behavior and not an error - Apple Silicon CPUs dynamically adjust their frequency based on workload.

## Command Definitions

The following commands are available for command-line or RPC invocation:

- `cpu.clockspeed` - Get full ClockSpeed object
- `cpu.clockspeed.mhz` - Get speed in MHz as double
- `cpu.clockspeed.ghz` - Get speed in GHz as double
- `cpu.clockspeed.formatted` - Get formatted string representation

## Return Values

### ClockSpeed Object

The `ClockSpeed` class contains:
- `speedMHz` (double) - Clock speed in megahertz
- `speedGHz` (double) - Clock speed in gigahertz (speedMHz / 1000)
- `source` (String) - The method/source used to determine the speed

### Null Returns

Methods return `null` or `-1.0` (for numeric methods) when:
- The platform is not supported (not Linux or Mac)
- The detection method fails
- The information is not available (e.g., Apple Silicon Macs)

## Error Handling

The utility handles errors gracefully:
- Logs warnings for unsupported platforms
- Logs errors for I/O failures or parsing issues
- Returns `null` or `-1.0` instead of throwing exceptions

## Example Output

### Intel Mac
```
2.60 GHz (2600 MHz) [sysctl machdep.cpu.brand_string]
```

### Linux System
```
3.40 GHz (3400 MHz) [/proc/cpuinfo]
```

### Apple Silicon Mac
```
Unknown
```
(With log message: "Apple Silicon detected: Apple M4 Max (dynamic frequency scaling - no fixed clock speed)")

## Dependencies

- Standard Java I/O libraries
- `com.hitorro.util.core.Platform` - For platform detection
- `com.hitorro.util.log.Logger` - For logging (via `Log.util`)

## License

Copyright (c) 2006-2025 Chris Collins - MIT License
