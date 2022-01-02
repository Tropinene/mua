echo "[bash run]"
cd src
cat ./in | java mua/Main
rm mua/*.class
