#!/usr/bin/env bash

# Colors and formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Print header
echo -e "${BOLD}===============================================${NC}"
echo -e "${BLUE}               Duke Test Runner               ${NC}"
echo -e "${BOLD}===============================================${NC}\n"

# Print Java version for debugging
echo -e "${BOLD}[1/4] Checking Java version...${NC}"
java -version

# check if using Java 21
java_ver=$(java -version 2>&1 | grep -i version | head -n 1)
echo -e "${BLUE}Detected Java version: ${NC}$java_ver"

version=$(echo $java_ver | sed -n 's/.*version "\([0-9]*\).*/\1/p')
echo -e "${BLUE}Parsed major version: ${NC}$version"

if [ "$version" != "21" ]; then
    echo -e "\n${RED}╔════ ERROR ══════════════════════════════════╗${NC}"
    echo -e "${RED}║ Please use Java 21 (current version: $version)${NC}"
    echo -e "${RED}╚═════════════════════════════════════════════╝${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Java version check passed${NC}\n"

# delete output from previous run
echo -e "${BOLD}[2/4] Setting up test environment...${NC}"
if [ -e "./ACTUAL.TXT" ]
then
    rm ACTUAL.TXT
    echo -e "${BLUE}Cleaned up previous test outputs${NC}"
fi

# build the project using Gradle (produces fat JAR with all dependencies)
echo -e "\n${BOLD}[3/4] Building with Gradle...${NC}"
if ! (cd .. && ./gradlew shadowJar -q)
then
    echo -e "\n${RED}╔════ ERROR ══════════════════════════════════╗${NC}"
    echo -e "${RED}║            BUILD FAILURE                     ║${NC}"
    echo -e "${RED}╚═════════════════════════════════════════════╝${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Build successful${NC}\n"

# run the program, feed commands from input.txt file and redirect the output to the ACTUAL.TXT
echo -e "${BOLD}[4/4] Running tests...${NC}"
java -cp ../build/libs/javafx-duke.jar Duke < input.txt > ACTUAL.TXT

# convert to UNIX format
cp EXPECTED.TXT EXPECTED-UNIX.TXT
dos2unix ACTUAL.TXT EXPECTED-UNIX.TXT 2>/dev/null

# compare the output to the expected output
diff ACTUAL.TXT EXPECTED-UNIX.TXT
if [ $? -eq 0 ]
then
    echo -e "\n${GREEN}╔════ SUCCESS ════════════════════════════════╗${NC}"
    echo -e "${GREEN}║              All tests passed                ║${NC}"
    echo -e "${GREEN}╚═════════════════════════════════════════════╝${NC}"
    exit 0
else
    echo -e "\n${RED}╔════ ERROR ══════════════════════════════════╗${NC}"
    echo -e "${RED}║              Tests FAILED                     ║${NC}"
    echo -e "${RED}╚═════════════════════════════════════════════╝${NC}"
    exit 1
fi