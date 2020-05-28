import Flutter
import UIKit
import PsiphonTunnel
public class SwiftPsiphonPlugin: NSObject, FlutterPlugin , TunneledAppDelegate {
    
    var configText = ""
    var result : FlutterResult?
    var connected = false
    var port = 0
    var psiphonTunnel:PsiphonTunnel?
    public func getPsiphonConfig() -> Any? {
        return configText
    }
    
    public func getEmbeddedServerEntries() -> String? {
        
        return ""
    }
    
    public func onDiagnosticMessage(_ message: String, withTimestamp timestamp: String) {
        NSLog("onDiagnosticMessage(%@): %@", timestamp, message)
    }
    
    public func onConnecting(){
        NSLog("onConnecting")
    }
    
    
    public func onConnectionStateChanged(from oldState: PsiphonConnectionState, to newState: PsiphonConnectionState) {
        
        
        switch newState {
        case PsiphonConnectionState.connected:
            result?(self.getJsonConnect(connected: true, port: self.port))
            break
        case PsiphonConnectionState.disconnected:
            result?(self.getJsonConnect(connected: false, port: 0))
            break
            
        default:
            break
        }
        
        
    }
    public func onConnected() {
        NSLog("onConnected")
        
        connected = true
        
        
    }
    
    
    public func onListeningHttpProxyPort(_ port: Int) {
        self.port = port
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "psiphon", binaryMessenger: registrar.messenger())
        let instance = SwiftPsiphonPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
        
        switch call.method {
        case "connect":
            self.result = result
            
            if self.psiphonTunnel == nil{
                self.psiphonTunnel =  PsiphonTunnel.newPsiphonTunnel(self)
            }
            
        

             let args = call.arguments as? Dictionary<String, Any>
            configText = args?["configText"] as! String
             
            
            self.psiphonTunnel?.start(true)
            
        case "connectionState":
            self.connectionState(result: result)
            break
            
        case "stop":
            self.stop(result: result)
            break
        default:
            result("Method no found")
        }
    }
    
    private func connectionState(result : FlutterResult){
        let state = self.psiphonTunnel?.getConnectionState()
        result(state?.rawValue)
    }
    
    private func getJsonConnect(connected : Bool,port : Int) -> String {
        let object = ConnetStatus(connected: connected, port: port)
        let jsonData = try! JSONEncoder().encode(object)
        let jsonString = String(data: jsonData, encoding: .utf8)!
        
        return jsonString
    }
    
    private func stop(result : FlutterResult){
        self.psiphonTunnel?.stop()
        result(nil)
    }
    
}

struct ConnetStatus: Codable {
    let connected: Bool
    let port: Int
}

