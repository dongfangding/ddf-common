#!/bin/sh

# 查看有哪些人哪些邮箱提交过记录
# git log | grep "^Author: " | awk '{print $2 $3}' | sort | uniq -c

git filter-branch -f --env-filter '
OLD_EMAIL="dingdongfang@runxsports.com"
CORRECT_NAME="dongfang.ding"
CORRECT_EMAIL="1041765757@qq.com"
if [ "$GIT_COMMITTER_EMAIL" = "$OLD_EMAIL" ]
then
    export GIT_COMMITTER_NAME="$CORRECT_NAME"
    export GIT_COMMITTER_EMAIL="$CORRECT_EMAIL"
fi
if [ "$GIT_AUTHOR_EMAIL" = "$OLD_EMAIL" ]
then
    export GIT_AUTHOR_NAME="$CORRECT_NAME"
    export GIT_AUTHOR_EMAIL="$CORRECT_EMAIL"
fi
' --tag-name-filter cat -- --branches --tags

# 最后提交历史记录
# git push --force --tags origin 'refs/heads/*'