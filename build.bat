@echo off
chcp 65001 >nul
REM FG 系列 Minecraft 插件一键编译脚本 (Windows)
REM 用法: 双击运行 build.bat

setlocal enabledelayedexpansion
set SCRIPT_DIR=%~dp0
set BUILD_DIR=%SCRIPT_DIR%build
set LIBS_DIR=%SCRIPT_DIR%libs
set JAVAC=javac
set JAR=jar

echo ========================================
echo   FG 系列 Minecraft 插件编译工具
echo ========================================
echo.

REM 检查Java
where javac >nul 2>nul
if errorlevel 1 (
    echo 错误: 未找到javac，请安装JDK 17或更高版本
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)

echo Java版本:
java -version 2>&1 | findstr "version"
echo.

REM 检查spigot-api
set SPIGOT_API=%LIBS_DIR%\spigot-api-1.20.1.jar
if not exist "%SPIGOT_API%" (
    echo 未找到spigot-api，请手动下载:
    echo   1. 访问 https://www.spigotmc.org/
    echo   2. 下载 spigot-api-1.20.1-R0.1-SNAPSHOT.jar
    echo   3. 放到 %LIBS_DIR%\spigot-api-1.20.1.jar
    echo.
    pause
    exit /b 1
)

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

set PLUGINS=SimpleVeinMiner FG_Economy FG-AIHelper GameModeLock FG-Domain

for %%P in (%PLUGINS%) do (
    echo.
    echo ========== 编译 %%P ==========
    set SRC_DIR=%SCRIPT_DIR%%%P\src
    set OUT_DIR=%BUILD_DIR%\%%P
    if not exist "!OUT_DIR!" mkdir "!OUT_DIR!"

    REM 收集源文件
    dir /s /b "!SRC_DIR!\*.java" > "!OUT_DIR!\sources.txt" 2>nul
    if not exist "!OUT_DIR!\sources.txt" (
        echo   跳过: 没有找到Java源文件
        continue
    )

    REM 编译
    "%JAVAC%" -encoding UTF-8 -cp "%SPIGOT_API%" -d "!OUT_DIR!" @"!OUT_DIR!\sources.txt"
    if errorlevel 1 (
        echo   错误: 编译失败
        continue
    )

    REM 打包jar
    copy "!SRC_DIR!\plugin.yml" "!OUT_DIR!\plugin.yml" >nul
    pushd "!OUT_DIR!"
    "%JAR%" cf "%BUILD_DIR%\%%P.jar" plugin.yml $(dir /s /b *.class)
    popd

    echo   成功: %BUILD_DIR%\%%P.jar
)

echo.
echo ========================================
echo   编译完成！
echo ========================================
echo.
echo 所有插件jar文件在: %BUILD_DIR%\
dir /b "%BUILD_DIR%\*.jar" 2>nul
echo.
echo 将jar文件复制到服务器的plugins\目录，重启服务器即可使用。
echo.
pause