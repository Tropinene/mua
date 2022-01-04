echo "[bash run]"
cd src
cat ./in | java mua/Main
# java mua/Main
rm mua/*.class
