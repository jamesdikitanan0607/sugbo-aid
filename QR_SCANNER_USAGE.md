# QR Scanner Integration for Inventory Updates

## Overview

The QR scanner integration allows users to update inventory stock levels by scanning specially formatted QR codes. This feature is implemented in task 7.3 and provides a quick and efficient way to manage inventory without manual data entry.

## QR Code Format

The QR scanner expects QR codes in the following format:
```
INVENTORY:ItemName:Quantity:Operation
```

### Components:
- **INVENTORY**: Fixed prefix to identify inventory QR codes
- **ItemName**: Name of the inventory item (e.g., "Rice", "Water", "Medicine")
- **Quantity**: Positive integer representing the quantity to add or remove
- **Operation**: Either "ADD" or "REMOVE" (case-insensitive)

### Examples:
- `INVENTORY:Rice:50:ADD` - Add 50 units of Rice
- `INVENTORY:Water:25:REMOVE` - Remove 25 units of Water
- `INVENTORY:Medicine:10:ADD` - Add 10 units of Medicine

## How to Use

### From Inventory Screen:
1. Navigate to the Inventory screen
2. Tap the QR scanner button (📷 icon)
3. Grant camera permission if prompted
4. Point the camera at a valid inventory QR code
5. The scanner will automatically detect and process the QR code
6. Confirm the inventory update in the dialog that appears
7. The inventory will be updated automatically

### QR Scanner Features:
- **Automatic Detection**: Scans QR codes continuously until a valid one is found
- **Validation**: Only accepts properly formatted inventory QR codes
- **Error Handling**: Shows clear error messages for invalid QR codes
- **Confirmation**: Always asks for user confirmation before applying updates
- **Feedback**: Provides success/error feedback after operations

## Error Handling

### Camera Permission:
- If camera permission is denied, the app will show a dialog explaining why the permission is needed
- Users can be redirected to app settings to grant permission manually

### Invalid QR Codes:
- Non-inventory QR codes are rejected with an error message
- Malformed QR codes show specific error details
- Users can retry scanning after errors

### Network/Data Issues:
- Inventory updates work offline using local storage
- Changes are persisted immediately in SharedPreferences
- No network connection required for basic functionality

## Testing QR Codes

For testing purposes, you can generate QR codes using online QR generators with these sample contents:

### Sample QR Codes:
1. `INVENTORY:Rice:50:ADD`
2. `INVENTORY:Water:25:REMOVE`
3. `INVENTORY:Medicine:10:ADD`
4. `INVENTORY:Clothes:15:REMOVE`
5. `INVENTORY:Canned Goods:30:ADD`

### Testing Steps:
1. Generate QR codes using any online QR code generator
2. Use the sample contents above
3. Display the QR code on another device or print it
4. Use the app's QR scanner to scan the test codes
5. Verify that inventory updates are applied correctly

## Implementation Details

### Files Created/Modified:
- `QRScannerActivity.java` - Main QR scanning activity
- `QRCodeUtils.java` - QR code parsing and validation utilities
- `QRCodeGenerator.java` - QR code generation utilities (for testing)
- `InventoryFragment.java` - Updated to integrate QR scanner
- Layout files for QR scanner UI
- Test files for QR code functionality

### Key Features:
- ZXing library integration for QR code scanning
- Custom QR scanner activity with camera preview
- Comprehensive error handling and user feedback
- Input validation and data sanitization
- Offline functionality with SharedPreferences storage
- Unit tests for QR code parsing logic

### Security Considerations:
- QR codes are validated before processing
- Only specific inventory format is accepted
- No arbitrary code execution from QR content
- Camera permission is properly requested and handled

## Future Enhancements

Potential improvements for future versions:
- Batch QR code scanning for multiple items
- QR code generation within the app
- Barcode scanning support (in addition to QR codes)
- Integration with external inventory management systems
- Audit trail for QR-based inventory changes
- Custom QR code formats for different operations