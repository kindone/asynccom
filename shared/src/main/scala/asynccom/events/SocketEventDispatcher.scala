package com.kindone.asynccom.events

import com.kindone.event.{ EventDispatcher, EventListener }

/**
 * Created by kindone on 2016. 5. 30..
 */
class SocketEventDispatcher extends SocketOpenCloseEventDispatcher
  with MessageReceiveEventDispatcher with SocketErrorEventDispatcher
