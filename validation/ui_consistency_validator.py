#!/usr/bin/env python3
"""
SugboAid Android App - UI Consistency Validation Script

This script validates UI consistency between the Android app and original TSX application
by checking color schemes, dimensions, and layout properties.
"""

import json
import xml.etree.ElementTree as ET
import os
import re
from typing import Dict, List, Tuple

class UIConsistencyValidator:
    def __init__(self, project_root: str):
        self.project_root = project_root
        self.app_path = os.path.join(project_root, "app", "src", "main")
        self.res_path = os.path.join(self.app_path, "res")
        
        # Expected color values from original TSX app
        self.expected_colors = {
            "primary_blue": "#1E4C82",
            "primary_green": "#2CB67D", 
            "accent_yellow": "#FDB813",
            "light_blue": "#2563eb",
            "success_green": "#10b981",
            "warning_orange": "#f59e0b",
            "error_red": "#ef4444"
        }
        
        # Expected dimensions and spacing
        self.expected_dimensions = {
            "card_corner_radius": "12dp",
            "button_corner_radius": "8dp",
            "standard_margin": "16dp",
            "small_margin": "8dp",
            "large_margin": "24dp"
        }
        
        self.validation_results = []

    def validate_colors(self) -> List[Dict]:
        """Validate color consistency across the app"""
        results = []
        colors_file = os.path.join(self.res_path, "values", "colors.xml")
        
        if not os.path.exists(colors_file):
            results.append({
                "type": "error",
                "category": "colors",
                "message": "colors.xml file not found"
            })
            return results
        
        try:
            tree = ET.parse(colors_file)
            root = tree.getroot()
            
            defined_colors = {}
            for color in root.findall("color"):
                name = color.get("name")
                value = color.text
                defined_colors[name] = value
            
            # Check if expected colors are defined
            for expected_name, expected_value in self.expected_colors.items():
                if expected_name in defined_colors:
                    actual_value = defined_colors[expected_name]
                    if actual_value.upper() == expected_value.upper():
                        results.append({
                            "type": "success",
                            "category": "colors",
                            "message": f"Color {expected_name} matches expected value: {expected_value}"
                        })
                    else:
                        results.append({
                            "type": "warning",
                            "category": "colors", 
                            "message": f"Color {expected_name} mismatch. Expected: {expected_value}, Actual: {actual_value}"
                        })
                else:
                    results.append({
                        "type": "error",
                        "category": "colors",
                        "message": f"Expected color {expected_name} not defined"
                    })
            
            # Check for glassmorphism colors
            glassmorphism_colors = [
                "glass_white_60", "glass_wh