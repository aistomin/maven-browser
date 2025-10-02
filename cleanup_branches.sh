#!/bin/bash

# Get current branch name
current_branch=$(git branch --show-current)

# Fetch latest changes and prune deleted remote branches
git fetch --prune

# Find local branches that don't have corresponding remote branches
# (either no tracking info or tracking a deleted remote branch)
# and exclude the current branch from deletion
git branch -vv | \
  awk '{
    # Skip the current branch (starts with *)
    if ($1 == "*") next
    
    # Check if branch has no tracking info or tracking a deleted remote branch
    # For non-current branches: $1=branch_name, $2=commit, $3=tracking_info
    # For current branch: $1=*, $2=branch_name, $3=commit, $4=tracking_info
    tracking_info = $3 " " $4
    if (tracking_info == "" || tracking_info ~ /: gone/) {
      print $1
    }
  }' | \
  grep -v "^$current_branch$" | \
  xargs -r git branch -D

echo "Cleanup completed. Current branch is '$current_branch'."
