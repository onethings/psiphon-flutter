import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:psiphon/connectStatus.dart';

class Psiphon {
  static const MethodChannel _channel = const MethodChannel('psiphon');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<ConnectStatus> get connect async {

    String psiphonConfig = await rootBundle.loadString('assets/psiphon_config.json');
    final String status = await _channel.invokeMethod("connect", {"configText": psiphonConfig});
    var connectStatus = connectStatusFromJson(status);
    return connectStatus;
  }


  static Future<void> stop() async {
    await _channel.invokeMethod('stop');
  }

  static Future<PsiphonConnectionState> get connectionState async {
    final int status = await _channel.invokeMethod('connectionState');
    PsiphonConnectionState connState;

    switch (status) {
      case 0:
        connState = PsiphonConnectionState.disconnected;
        break;
      case 1:
        connState = PsiphonConnectionState.connecting;
        break;
      case 2:
        connState = PsiphonConnectionState.connected;
        break;
      case 3:
        connState = PsiphonConnectionState.waitingForNetwork;
        break;
    }

    return connState;
  }
}

enum PsiphonConnectionState {
  disconnected,
  connecting,
  connected,
  waitingForNetwork
}
