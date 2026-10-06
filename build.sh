#!/bin/bash
# FG 系列 Minecraft 插件一键编译脚本 (Linux/macOS)
# 用法: ./build.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$SCRIPT_DIR/build"
LIBS_DIR="$SCRIPT_DIR/libs"
JAVAC="javac"
JAR="jar"

# 检查Java
if ! command -v javac &> /dev/null; then
    echo "错误: 未找到javac，请安装JDK 17或更高版本"
    exit 1
fi

echo "Java版本:"
java -version 2>&1 | head -1

# 下载spigot-api（如果不存在）
SPIGOT_API="$LIBS_DIR/spigot-api-1.20.1.jar"
if [ ! -f "$SPIGOT_API" ] || [ $(stat -c%s "$SPIGOT_API" 2>/dev/null || echo 0) -lt 100000 ]; then
    echo "正在下载spigot-api 1.20.1..."
    mkdir -p "$LIBS_DIR"
    # 尝试多个源
    curl -sL -o "$SPIGOT_API" "https://repo.papermc.io/repository/maven-public/org/spigotmc/spigot-api/1.20.1-R0.1-SNAPSHOT/spigot-api-1.20.1-R0.1-SNAPSHOT.jar" 2>/dev/null || true
    if [ ! -f "$SPIGOT_API" ] || [ $(stat -c%s "$SPIGOT_API" 2>/dev/null || echo 0) -lt 100000 ]; then
        curl -sL -o "$SPIGOT_API" "https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/1.20.1-R0.1-SNAPSHOT/spigot-api-1.20.1-R0.1-SNAPSHOT.jar" 2>/dev/null || true
    fi
    if [ ! -f "$SPIGOT_API" ] || [ $(stat -c%s "$SPIGOT_API" 2>/dev/null || echo 0) -lt 100000 ]; then
        echo "警告: spigot-api下载失败，请手动下载到 libs/spigot-api-1.20.1.jar"
        echo "下载地址: https://www.spigotmc.org/"
        exit 1
    fi
    echo "spigot-api下载完成 ($(stat -c%s "$SPIGOT_API") 字节)"
fi

mkdir -p "$BUILD_DIR"

# 编译每个插件
PLUGINS=("SimpleVeinMiner" "FG_Economy" "FG-AIHelper" "GameModeLock" "FG-Domain")

for plugin in "${PLUGINS[@]}"; do
    echo ""
    echo "========== 编译 $plugin =========="
    SRC_DIR="$SCRIPT_DIR/$plugin/src"
    OUT_DIR="$BUILD_DIR/$plugin"
    mkdir -p "$OUT_DIR"

    # 编译
    find "$SRC_DIR" -name "*.java" > "$OUT_DIR/sources.txt"
    if [ ! -s "$OUT_DIR/sources.txt" ]; then
        echo "  跳过: 没有找到Java源文件"
        continue
    fi

    $JAVAC -encoding UTF-8 -cp "$SPIGOT_API" -d "$OUT_DIR" @"$OUT_DIR/sources.txt"
    if [ $? -ne 0 ]; then
        echo "  错误: 编译失败"
        continue
    fi

    # 打包jar
    cp "$SRC_DIR/plugin.yml" "$OUT_DIR/"
    cd "$OUT_DIR"
    $JAR cf "$BUILD_DIR/${plugin}.jar" plugin.yml $(find . -name "*.class")
    cd "$SCRIPT_DIR"

    echo "  成功: $BUILD_DIR/${plugin}.jar ($(stat -c%s "$BUILD_DIR/${plugin}.jar" 2>/dev/null) 字节)"
done

echo ""
echo "========== 编译完成 =========="
echo "所有插件jar文件在: $BUILD_DIR/"
ls -la "$BUILD_DIR/"*.jar 2>/dev/null
echo ""
echo "将jar文件复制到服务器的plugins/目录，重启服务器即可使用。"