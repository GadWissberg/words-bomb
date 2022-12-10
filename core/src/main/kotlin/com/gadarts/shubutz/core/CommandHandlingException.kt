package com.gadarts.shubutz.core

class CommandHandlingException(val commandIndex: Int, msg: String) : Exception(msg)
