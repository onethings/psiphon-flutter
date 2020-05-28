import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';

import 'package:psiphon/psiphon.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  var _text = "Here info about connection";
  var _status = "Here info about status connection";

  @override
  void initState() {
    super.initState();

  }


  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body:  Container(
          padding: EdgeInsets.all(30),
          child: Column(
              children: <Widget>[
                Text(_text),
                SizedBox(height: 20),
                Text(_status),
                SizedBox(height: 50),
                FlatButton(
                    onPressed: () async {
                      var status = await Psiphon.connect;

                      if (status.connected) {
                        HttpClient client = new HttpClient();
                        client.findProxy = (Uri uri) {
                          return 'PROXY 127.0.0.1:${status.port};';
                        };

                        var c = await client
                            .getUrl(Uri.parse("http://ip-api.com/json/"));

                        final response = await c.close();

                        response.transform(utf8.decoder).listen((contents) {
                          _text = contents;
                          setState(() {});
                        });



                      }
                    },
                    child: Text("Connect with psiphon")),
                FlatButton(
                    onPressed: () async {

                        HttpClient client = new HttpClient();
                        var c = await client
                            .getUrl(Uri.parse("http://ip-api.com/json/"));

                        final response = await c.close();

                        response.transform(utf8.decoder).listen((contents) {
                          _text = contents;
                          setState(() {});
                        });

                    },
                    child: Text("Connect without psiphon")),
                FlatButton(
                  onPressed: () async {
                    PsiphonConnectionState state = await Psiphon.connectionState;

                    setState(() {
                      _status = state.toString();
                    });
                  },
                  child: Text("get status"),
                ),
                FlatButton(
                  onPressed: () async {
                     Psiphon.stop();
                  },
                  child: Text("Stop"),
                ),
              ],
            ),
        ),
        ),
     
    );
  }
}
