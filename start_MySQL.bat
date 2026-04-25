@echo off
:: 设置窗口标题，更清晰
title MySQL9.7 启动脚本
:: 清屏
cls
echo ==============================================
echo           MySQL 9.7.0 自动启动工具
echo ==============================================
echo.

:: 跨盘符切换到MySQL的bin目录
cd /d D:\mysql-9.7.0-winx64\bin
echo 已切换至MySQL安装目录：%cd%
echo.

:: 启动MySQL服务（服务名：mysql97）
echo 正在启动 MySQL97 服务...
net start mysql97
echo.

:: 自动连接MySQL，直接传入密码（-p后紧跟密码，无空格）
echo 正在自动连接 MySQL 服务器(用户：root，密码已自动填充)...
echo.
.\mysql -u root -p25844796

:: 如果连接失败，暂停窗口查看错误
if errorlevel 1 (
    echo.
    echo 连接失败！请检查服务状态、用户名或密码
    pause
)