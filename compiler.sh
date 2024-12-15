#!/bin/bash

# Function to display usage
usage() {
  echo "Usage: $0 [main-class]"
  echo ""
  echo "Options:"
  echo "  [main-class]   Fully qualified name of the main class to run (optional)"
  echo ""
  echo "Example:"
  echo "  $0 ixs190023.TestCaseRunner"
}

# Prompt user for confirmation to use the current directory
echo "Do you want to use the current directory ($(pwd)) for source and output? (y/n)"
read -r choice
if [[ "$choice" != "y" && "$choice" != "Y" ]]; then
  echo "Exiting. Please run the script from the desired directory."
  exit 0
fi

# Default source and output directory (CWD)
SRC_DIR=$(pwd)
OUT_DIR=$(pwd)

# Get the main class from arguments
MAIN_CLASS="$1"

# Clean old .class files
echo "Cleaning old .class files in $OUT_DIR..."
find "$OUT_DIR" -type f -name "*.class" -delete
echo "Old .class files removed."

# Compile all Java files in the current directory
echo "Compiling Java files in $SRC_DIR..."
javac -d "$OUT_DIR" "$SRC_DIR"/*.java
if [ $? -ne 0 ]; then
  echo "Compilation failed."
  exit 1
fi
echo "Compilation successful. Classes saved in $OUT_DIR."

# Run the specified main class, if provided
if [ -n "$MAIN_CLASS" ]; then
  echo "Running main class: $MAIN_CLASS"
  java -cp "$OUT_DIR" "$MAIN_CLASS"
fi
