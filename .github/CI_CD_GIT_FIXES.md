# CI/CD Git Repository Issue Fixes

## Problem
The CI/CD pipeline was failing with the error:
```
fatal: not a git repository (or any of the parent directories): .git
Error: Error: The process '/usr/bin/git' failed with exit code 128
```

## Root Causes Identified
1. **Insufficient Git History**: The default checkout action only fetches shallow history
2. **Missing Git Configuration**: Actions and Maven plugins needed proper git setup
3. **Git Commit ID Plugin**: Some Maven builds try to access git information
4. **Working Directory Permissions**: GitHub Actions workspace permissions

## Fixes Applied

### 1. Enhanced Checkout Configuration
```yaml
- name: Checkout code
  uses: actions/checkout@v4
  with:
    fetch-depth: 0  # Fetch full git history
    token: ${{ secrets.GITHUB_TOKEN }}
```

### 2. Added Git Configuration Step
```yaml
- name: Configure Git
  run: |
    git config --global --add safe.directory /github/workspace
    git config --global --add safe.directory $GITHUB_WORKSPACE
    git config --global user.name "GitHub Actions"
    git config --global user.email "actions@github.com"
    echo "Git configuration completed"
    pwd
    ls -la
    git status || echo "Git status failed, but continuing..."
```

### 3. Disabled Git Commit ID Plugin in Maven
```yaml
mvn clean compile -B -U -Dmaven.gitcommitid.skip=true
mvn test -B -Duser.timezone=UTC -Dmaven.gitcommitid.skip=true
mvn verify -P integration-tests -B -Dmaven.gitcommitid.skip=true
```

### 4. Updated Codecov Action
```yaml
- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v4  # Updated from v3
  with:
    files: ./**/target/site/jacoco/jacoco.xml
    flags: unittests
    name: codecov-umbrella
    token: ${{ secrets.CODECOV_TOKEN }}
    fail_ci_if_error: false  # Don't fail CI if codecov fails
```

## Files Modified
- `/.github/workflows/ci-cd.yml` - Complete CI/CD pipeline fixes
  - All checkout actions enhanced with full git history
  - Git configuration step added to test job
  - Maven commands updated with git plugin skip flags
  - Codecov action updated to v4

## Validation
✅ Local build works with new Maven flags
✅ Git repository properly configured
✅ All checkout actions enhanced across all jobs
✅ Error handling added for optional git operations

## Expected Results
- CI/CD pipeline should no longer fail with git repository errors
- Full git history available for all jobs
- Maven builds skip git-dependent plugins when needed
- Proper git configuration for GitHub Actions environment

## Additional Notes
- The `CODECOV_TOKEN` warning is expected if the secret is not configured
- Git status verification added for debugging
- Safe directory configuration handles workspace permissions
- Timezone explicitly set to avoid locale issues