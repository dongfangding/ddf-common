fromBranch=$1
targetBranch=$2

if [ ! "${fromBranch}" ] ; then
  echo "请输入要merge的from分支"
  exit
fi;

if [ ! "${targetBranch}" ] ; then
  echo "请输入要merge的to分支"
  exit
fi;

git checkout "${targetBranch}"
git pull origin "${targetBranch}"
git merge "${fromBranch}"
git push origin "${targetBranch}"
git checkout "${fromBranch}"
git pull origin "${fromBranch}"
