#!/bin/bash

# 子模块
targetBranch=$1
deploy=$2
maven_settings="D:/develop_tools/apache-maven-3.9.9/conf/oneprice-settings.xml"

deploy_project() {
    if [ -z "$deploy" ]; then
        echo "⚠️ 未提供 deploy 参数，跳过执行。"
        return
    fi

    # 切换到项目根目录
    cd "$deploy" || exit 1
    echo "🚀 执行 Maven Deploy ..."
    mvn --settings "$maven_settings" -U clean deploy
    cd ..
}

if [ ! "${targetBranch}" ] ; then
  echo "请输入要merge的to分支"
  exit
fi;


# 获取当前分支名
fromBranch=$(git rev-parse --abbrev-ref HEAD)

# 【关键防护】获取当前本地状态
local_status=$(git status --porcelain)

# 先输出当前状态内容（即使为空也会显示一行提示，方便观察）
echo "--- 当前 Git 本地状态检核 ---"
if [ -z "$local_status" ]; then
    echo "✨ 工作区干净，未发现未提交的修改。"
else
    echo "⚠️ 发现以下未提交的内容："
    echo "$local_status"
fi
echo "--------------------------"

# 再进行逻辑阻断判断
if [ -n "$local_status" ]; then
    echo "❌ 拦截：当前分支有未提交的修改，请先 commit 或 stash，否则会污染目标分支。"
    exit 1
fi

echo "开始合并：${fromBranch} -> ${targetBranch}"

# 1. 切换并更新目标分支
git checkout "${targetBranch}" || exit 1
git pull origin "${targetBranch}"

# 2. 合并开发分支到目标分支
echo "执行合并操作..."
if git merge "origin/${fromBranch}" -m "merge ${fromBranch}"; then
    echo "✅ 合并成功，正在推送..."
    git push origin "${targetBranch}"

    # 3. 执行部署
    deploy_project

    # 4. 成功后切回原分支
    echo "正在切回原开发分支: ${fromBranch}"
    git checkout "${fromBranch}"
    # 这里不再执行 git pull，避免任何潜在的远程污染
else
    echo "----------------------------------------------------------------"
    echo "❌ 发现代码冲突！"
    echo "请在 IDEA 中手动解决 [${targetBranch}] 的冲突。"
    echo "解决并 commit/push 后，请手动切回 [${fromBranch}] 分支。"
    echo "----------------------------------------------------------------"
    exit 1
fi
