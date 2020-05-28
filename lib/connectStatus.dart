import 'dart:convert';

ConnectStatus connectStatusFromJson(String str) => ConnectStatus.fromJson(json.decode(str));

String connectStatusToJson(ConnectStatus data) => json.encode(data.toJson());

class ConnectStatus {
  bool connected;
  int port;

  ConnectStatus({
    this.connected,
    this.port,
  });

  factory ConnectStatus.fromJson(Map<String, dynamic> json) => ConnectStatus(
    connected: json["connected"],
    port: json["port"],
  );

  Map<String, dynamic> toJson() => {
    "connected": connected,
    "port": port,
  };
}
