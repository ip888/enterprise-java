#!/bin/bash

# Quick Pre-Push CI/CD Validation
# Tests the essential components before GitHub push

echo "🚀 Enterprise Java - Pre-Push Validation"
echo "========================================="

EXIT_CODE=0

# Test 1: Maven Build
echo "🏗️  Testing Maven Build..."
cd /workspaces/enterprise-java
if mvn clean compile -q; then
    echo "✅ Maven compilation successful"
else
    echo "❌ Maven compilation failed"
    EXIT_CODE=1
fi

# Test 2: Unit Tests  
echo "🧪 Running Unit Tests..."
if mvn test -q; then
    echo "✅ All unit tests passed"
else
    echo "❌ Unit tests failed"
    EXIT_CODE=1
fi

# Test 3: Package Build
echo "📦 Testing Package Creation..."
if mvn package -DskipTests -q; then
    echo "✅ Package creation successful"
else
    echo "❌ Package creation failed" 
    EXIT_CODE=1
fi

# Test 4: Configuration Validation
echo "⚙️  Validating Configurations..."
cd infrastructure/monitoring
if docker-compose config >/dev/null 2>&1; then
    echo "✅ Monitoring configuration valid"
else
    echo "❌ Monitoring configuration invalid"
    EXIT_CODE=1
fi

cd ../..
cd streaming-analytics
if docker-compose config >/dev/null 2>&1; then
    echo "✅ Infrastructure configuration valid"
else
    echo "❌ Infrastructure configuration invalid"
    EXIT_CODE=1
fi

# Test 5: Check Management Scripts
echo "📋 Checking Management Scripts..."
cd ..
for script in "run.sh" "project-manager.sh" "health-check.sh"; do
    if [ -x "./$script" ]; then
        echo "✅ $script is executable"
    else
        echo "❌ $script is missing or not executable"
        EXIT_CODE=1
    fi
done

# Test 6: Documentation Check
echo "📚 Checking Documentation..."
if [ -f "README.md" ] && [ -s "README.md" ]; then
    echo "✅ README.md exists and has content"
else
    echo "❌ README.md missing or empty"
    EXIT_CODE=1
fi

echo
echo "🔍 Pre-Push Validation Summary:"
echo "==============================="

if [ $EXIT_CODE -eq 0 ]; then
    echo "🎉 All checks passed! Project is ready for GitHub push."
    echo
    echo "Next steps:"
    echo "  git add ."
    echo "  git commit -m 'feat: complete enterprise java project with monitoring'"  
    echo "  git push origin main"
else
    echo "💥 Some checks failed. Please fix issues before pushing."
fi

exit $EXIT_CODE