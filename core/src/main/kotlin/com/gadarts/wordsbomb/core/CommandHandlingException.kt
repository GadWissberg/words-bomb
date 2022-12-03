package com.gadarts.wordsbomb.core

class CommandHandlingException(val commandIndex: Int, msg: String) : Exception(msg)
