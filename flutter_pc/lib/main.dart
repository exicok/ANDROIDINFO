import 'dart:io';
import 'dart:convert';
import 'dart:async';
import 'package:flutter/material.dart';

// --- Multi-language ---
class Strings {
  static String locale = 'zh';
  static const map = {
    'zh': {
      'app_name': '设备信息监控端',
      'devices': '发现设备',
      'settings': '软件设置',
      'home': '主页',
      'environment': '硬件页',
      'android_version': 'Android 版本',
      'api_level': 'API 级别',
      'bootloader': 'Bootloader',
      'mem_usage': '内存占用',
      'selinux_status': 'SELinux 状态',
      'current_time': '刷新时间',
      'root_modules': 'Root 模块',
      'storage_status': '存储状态',
      'manufacturer': '制造商',
      'battery_level': '电池电量',
      'soc_info': '处理器',
      'mem_total': '内存总量',
      'used': '已使用',
      'gpu_info': '图形信息 (GPU)',
      'port': '监听端口',
      'search_hint': '搜索型号、ID 或 IP...',
      'no_devices': '正在搜索广播设备...',
      'details': '详细信息',
      'system': '系统',
      'hardware': '硬件',
      'battery': '电池',
      'screen': '显示',
      'scrcpy': '投屏 (scrcpy)',
      'scrcpy_hint': '需确保设备开启 ADB 且电脑已安装 scrcpy',
      'tab_processor': '处理器',
      'tab_camera': '摄像头',
      'tab_graphics': '图形',
      'tab_sensors': '传感器',
      'tab_partitions': '分区',
      'wifi_info': 'Wi-Fi',
      'bt_info': '蓝牙',
      'audio_info': '音频',
      'kernel_version': '内核版本',
      'lang_settings': '语言',
      'theme_settings': '主题',
      'light': '浅色',
      'dark': '深色',
      'system_theme': '系统',
      'adb_port': '默认 ADB 端口',
    },
    'en': {
      'app_name': 'Device Monitor',
      'devices': 'Devices',
      'settings': 'Settings',
      'home': 'Home',
      'environment': 'Hardware',
      'android_version': 'Android Version',
      'api_level': 'API Level',
      'bootloader': 'Bootloader',
      'mem_usage': 'Memory',
      'selinux_status': 'SELinux',
      'current_time': 'Sync Time',
      'root_modules': 'Root Mods',
      'storage_status': 'Storage',
      'manufacturer': 'Manufacturer',
      'battery_level': 'Battery',
      'soc_info': 'Processor',
      'mem_total': 'Total Memory',
      'used': 'Used',
      'gpu_info': 'GPU Info',
      'port': 'Port',
      'search_hint': 'Search...',
      'scrcpy': 'Mirror Screen',
      'scrcpy_hint': 'Requires ADB enabled and scrcpy installed',
      'tab_processor': 'Processor',
      'tab_camera': 'Camera',
      'tab_graphics': 'Graphics',
      'tab_sensors': 'Sensors',
      'tab_partitions': 'Partitions',
      'wifi_info': 'Wi-Fi',
      'bt_info': 'Bluetooth',
      'audio_info': 'Audio',
      'adb_port': 'Default ADB Port',
    }
  };
  static String get(String key) => map[locale]?[key] ?? key;
}

final localeNotifier = ValueNotifier<String>('zh');
final themeNotifier = ValueNotifier<ThemeMode>(ThemeMode.system);
final portNotifier = ValueNotifier<int>(8888);
final adbPortNotifier = ValueNotifier<int>(5555);

void main() {
  runApp(const DeviceInfoPCApp());
}

class DeviceInfoPCApp extends StatelessWidget {
  const DeviceInfoPCApp({super.key});
  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<ThemeMode>(
      valueListenable: themeNotifier,
      builder: (c, mode, _) => ValueListenableBuilder<String>(
        valueListenable: localeNotifier,
        builder: (c, lang, _) {
          Strings.locale = lang;
          return MaterialApp(
            title: 'Android Monitor',
            debugShowCheckedModeBanner: false,
            themeMode: mode,
            theme: ThemeData(colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue), useMaterial3: true),
            darkTheme: ThemeData(colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue, brightness: Brightness.dark), useMaterial3: true),
            home: const MainNavigationScreen(),
          );
        },
      ),
    );
  }
}

class MainNavigationScreen extends StatefulWidget {
  const MainNavigationScreen({super.key});
  @override
  State<MainNavigationScreen> createState() => _MainNavigationScreenState();
}

class _MainNavigationScreenState extends State<MainNavigationScreen> {
  int _index = 0;
  @override
  Widget build(BuildContext context) {
    final desk = MediaQuery.of(context).size.width > 800;
    return Scaffold(
      body: Row(children: [
        if (desk) NavigationRail(
          selectedIndex: _index,
          onDestinationSelected: (i) => setState(() => _index = i),
          labelType: NavigationRailLabelType.all,
          destinations: [
            NavigationRailDestination(icon: const Icon(Icons.devices), label: Text(Strings.get('devices'))),
            NavigationRailDestination(icon: const Icon(Icons.settings), label: Text(Strings.get('settings'))),
          ],
        ),
        const VerticalDivider(width: 1),
        Expanded(child: IndexedStack(index: _index, children: const [BroadcastMonitorScreen(), SettingsScreen()])),
      ]),
      bottomNavigationBar: desk ? null : NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: [
          NavigationDestination(icon: const Icon(Icons.devices), label: Strings.get('devices')),
          NavigationDestination(icon: const Icon(Icons.settings), label: Strings.get('settings')),
        ],
      ),
    );
  }
}

class DiscoveredDevice {
  final List<String> p;
  String ip;
  int? customAdbPort;
  final DateTime lastSeen;
  DiscoveredDevice(this.p, this.ip, this.lastSeen);

  String at(int i) => i < p.length ? p[i] : "N/A";

  String get id => at(0);
  String get model => at(1);
  String get manufacturer => at(2);
  String get version => at(3);
  String get soc => at(4);
  String get bootloader => at(5);
  String get selinux => at(6);
  String get rootMods => at(7);
  String get storageTotal => at(8);
  String get storageUsed => at(9);
  String get memTotal => at(10);
  String get memUsed => at(11);
  String get batteryLevel => at(12);
  String get batteryStatus => at(13);
  String get batteryHealth => at(14);
  String get batteryVolt => at(15);
  String get batteryTemp => at(16);
  String get screenRes => at(17);
  String get screenRefresh => at(18);
  String get screenSize => at(19);
  String get cpuCores => at(20);
  String get updateTime => at(21);
  String get apiLevel => at(22);
  String get kernel => at(23);
  String get jvm => at(24);
  String get buildDate => at(25);
  String get hardware => at(26);
  String get opengl => at(27);
  String get vulkan => at(28);
  String get opencl => at(29);
  String get board => at(30);
  String get platform => at(31);
  String get product => at(32);
  String get deviceNode => at(33);
  String get displayId => at(34);
  String get wifi => at(35);
  String get bluetooth => at(36);
  String get audio => at(37);
  String get gpuRenderer => at(38);
  String get gpuVendor => at(39);
  String get cameras => at(40);
  String get sensors => at(41);
  String get partitions => at(42);

  int get effectiveAdbPort => customAdbPort ?? adbPortNotifier.value;
}

class BroadcastMonitorScreen extends StatefulWidget {
  const BroadcastMonitorScreen({super.key});
  @override
  State<BroadcastMonitorScreen> createState() => _BroadcastMonitorScreenState();
}

class _BroadcastMonitorScreenState extends State<BroadcastMonitorScreen> {
  RawDatagramSocket? _socket;
  final Map<String, DiscoveredDevice> _devices = {};
  String _search = "";

  @override
  void initState() {
    super.initState();
    _start();
    portNotifier.addListener(_start);
    Timer.periodic(const Duration(seconds: 1), (t) {
      if (mounted) setState(() => _devices.removeWhere((k, v) => DateTime.now().difference(v.lastSeen).inSeconds > 3));
    });
  }

  void _start() async {
    _socket?.close();
    try {
      _socket = await RawDatagramSocket.bind(InternetAddress.anyIPv4, portNotifier.value);
      _socket?.listen((e) {
        if (e == RawSocketEvent.read) {
          Datagram? dg = _socket?.receive();
          if (dg != null) {
            try {
              final raw = utf8.decode(dg.data);
              final d = DiscoveredDevice(raw.split('|'), dg.address.address, DateTime.now());
              if (_devices.containsKey(d.id)) {
                d.ip = _devices[d.id]!.ip;
                d.customAdbPort = _devices[d.id]!.customAdbPort;
              }
              setState(() => _devices[d.id] = d);
            } catch (e) {}
          }
        }
      });
    } catch (_) {}
  }

  @override
  void dispose() { _socket?.close(); portNotifier.removeListener(_start); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    final list = _devices.values.where((d) => d.model.toLowerCase().contains(_search) || d.ip.contains(_search)).toList();
    return Column(children: [
      Padding(padding: const EdgeInsets.all(16), child: TextField(
        decoration: InputDecoration(hintText: Strings.get('search_hint'), prefixIcon: const Icon(Icons.search), border: OutlineInputBorder(borderRadius: BorderRadius.circular(12))),
        onChanged: (v) => setState(() => _search = v.toLowerCase()),
      )),
      Expanded(child: list.isEmpty ? Center(child: Text(Strings.get('no_devices'))) : ListView.builder(
        itemCount: list.length,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemBuilder: (c, i) => _buildCard(list[i]),
      )),
    ]);
  }

  Widget _buildCard(DiscoveredDevice d) {
    return Card(child: ListTile(
      leading: const CircleAvatar(child: Icon(Icons.smartphone)),
      title: Text(d.model, style: const TextStyle(fontWeight: FontWeight.bold)),
      subtitle: Text('${d.manufacturer} • Android ${d.version} • ${d.ip}:${d.effectiveAdbPort}'),
      trailing: Text(d.batteryLevel, style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold)),
      onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => DeviceDetailsTabs(device: d))),
    ));
  }
}

class DeviceDetailsTabs extends StatelessWidget {
  final DiscoveredDevice device;
  const DeviceDetailsTabs({super.key, required this.device});

  @override
  Widget build(BuildContext context) {
    final tabs = [
      Strings.get('home'),
      Strings.get('system'),
      Strings.get('hardware'),
      Strings.get('tab_processor'),
      Strings.get('screen'),
      Strings.get('tab_camera'),
      Strings.get('tab_graphics'),
      Strings.get('tab_sensors'),
      Strings.get('tab_partitions'),
      Strings.get('battery'),
    ];

    return DefaultTabController(
      length: tabs.length,
      child: Scaffold(
        appBar: AppBar(
          title: Text(device.model),
          bottom: TabBar(
            isScrollable: true,
            tabs: tabs.map((t) => Tab(text: t)).toList(),
          ),
        ),
        body: TabBarView(children: [
          _HomeTab(d: device),
          _SystemTab(d: device),
          _HardwareTab(d: device),
          _ProcessorTab(d: device),
          _ScreenTab(d: device),
          _CameraTab(d: device),
          _GraphicsTab(d: device),
          _SensorsTab(d: device),
          _PartitionsTab(d: device),
          _BatteryTab(d: device),
        ]),
      ),
    );
  }
}

// --- Home Tab ---
class _HomeTab extends StatelessWidget {
  final DiscoveredDevice d;
  const _HomeTab({required this.d});

  void _launchScrcpy(BuildContext context) async {
    final bool isWin = Platform.isWindows;
    final String baseDir = File(Platform.resolvedExecutable).parent.path;
    final String adbPath = isWin ? '$baseDir/scrcpy/adb.exe' : '$baseDir/scrcpy/adb';
    final String scrcpyPath = isWin ? '$baseDir/scrcpy/scrcpy.exe' : '$baseDir/scrcpy/scrcpy';

    final String finalAdb = File(adbPath).existsSync() ? adbPath : (isWin ? 'scrcpy/adb.exe' : 'scrcpy/adb');
    final String finalScrcpy = File(scrcpyPath).existsSync() ? scrcpyPath : (isWin ? 'scrcpy/scrcpy.exe' : 'scrcpy/scrcpy');

    if (!File(finalScrcpy).existsSync()) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text("错误: 找不到投屏引擎\n期待路径: $finalScrcpy"),
        backgroundColor: Colors.red,
      ));
      return;
    }

    final String fullAddr = "${d.ip}:${d.effectiveAdbPort}";
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("正在连接 $fullAddr...")));

    try {
      await Process.run(finalAdb, ['connect', fullAddr]);
      await Process.start(finalScrcpy, [
        '-s', fullAddr,
        '--window-title', 'Monitor: ${d.model} ($fullAddr)',
        '--always-on-top',
        '--stay-awake'
      ]);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("启动失败: $e"), backgroundColor: Colors.red));
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListView(padding: const EdgeInsets.all(16), children: [
      _Header(d: d),
      const SizedBox(height: 12),
      Row(children: [
        Expanded(child: ElevatedButton.icon(
          onPressed: () => _launchScrcpy(context),
          icon: const Icon(Icons.screenshot_monitor),
          label: Text(Strings.get('scrcpy')),
          style: ElevatedButton.styleFrom(minimumSize: const Size(0, 50), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))),
        )),
        const SizedBox(width: 8),
        IconButton.filledTonal(
          onPressed: () => _editDeviceIp(context),
          icon: const Icon(Icons.edit),
          tooltip: "修改设备 IP/端口",
        )
      ]),
      const Padding(padding: EdgeInsets.only(top: 8), child: Center(child: Text("提示: 需确保设备开启网络调试", style: TextStyle(fontSize: 10, color: Colors.grey)))),
      const SizedBox(height: 16),
      LayoutBuilder(builder: (c, cs) {
        int count = cs.maxWidth > 900 ? 4 : (cs.maxWidth > 600 ? 3 : 2);
        return GridView.count(
          shrinkWrap: true, physics: const NeverScrollableScrollPhysics(),
          crossAxisCount: count, mainAxisSpacing: 8, crossAxisSpacing: 8, childAspectRatio: 2.8,
          children: [
            _Stat(Strings.get('android_version'), d.version, Icons.phone, Colors.blue),
            _Stat(Strings.get('api_level'), 'API ${d.apiLevel}', Icons.settings, Colors.orange),
            _Stat(Strings.get('bootloader'), d.bootloader, Icons.info, Colors.teal),
            _Stat(Strings.get('selinux_status'), d.selinux, Icons.lock, Colors.blue),
            _Stat(Strings.get('root_modules'), d.rootMods, Icons.list, Colors.purple),
            _Stat(Strings.get('current_time'), d.updateTime, Icons.access_time, Colors.green),
          ],
        );
      }),
      const SizedBox(height: 16),
      _Usage(Strings.get('mem_usage'), d.memTotal, d.memUsed, Colors.red),
      const SizedBox(height: 12),
      _Usage(Strings.get('storage_status'), d.storageTotal, d.storageUsed, Colors.blue),
    ]);
  }

  void _editDeviceIp(BuildContext context) {
    final tcIp = TextEditingController(text: d.ip);
    final tcPort = TextEditingController(text: d.effectiveAdbPort.toString());

    showDialog(context: context, builder: (c) => AlertDialog(
      title: const Text("手动指定连接信息"),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(controller: tcIp, decoration: const InputDecoration(labelText: "设备 IP 地址")),
          const SizedBox(height: 8),
          TextField(controller: tcPort, decoration: const InputDecoration(labelText: "ADB 端口"), keyboardType: TextInputType.number),
        ],
      ),
      actions: [
        TextButton(onPressed: () => Navigator.pop(c), child: const Text("取消")),
        TextButton(onPressed: () {
          d.ip = tcIp.text;
          d.customAdbPort = int.tryParse(tcPort.text);
          Navigator.pop(c);
        }, child: const Text("确定")),
      ],
    ));
  }
}

// --- Detail Tabs ---
class _SystemTab extends StatelessWidget {
  final DiscoveredDevice d; const _SystemTab({required this.d});
  @override
  Widget build(BuildContext context) => ListView(padding: const EdgeInsets.all(16), children: [
    _Sec(Strings.get('system'), [_T(Strings.get('android_version'), d.version), _T(Strings.get('api_level'), d.apiLevel), _T(Strings.get('bootloader'), d.bootloader), _T(Strings.get('jvm_version'), d.jvm), _T(Strings.get('build_date'), d.buildDate)]),
    _Sec(Strings.get('kernel_version'), [_T(Strings.get('kernel_version'), d.kernel)]),
  ]);
}

class _HardwareTab extends StatelessWidget {
  final DiscoveredDevice d; const _HardwareTab({required this.d});
  @override
  Widget build(BuildContext context) => ListView(padding: const EdgeInsets.all(16), children: [
    _Sec(Strings.get('hardware'), [_T('Model', d.model), _T('Manufacturer', d.manufacturer), _T('Board', d.board), _T('Hardware', d.hardware), _T('Platform', d.platform), _T('Product', d.product)]),
    _Sec(Strings.get('wifi_info'), d.wifi.split(';').map((s) => _T(s.split(': ')[0], s.split(': ').length > 1 ? s.split(': ')[1] : "")).toList()),
    _Sec(Strings.get('bt_info'), d.bluetooth.split(';').map((s) => _T(s.split(': ')[0], s.split(': ').length > 1 ? s.split(': ')[1] : "")).toList()),
    _Sec(Strings.get('audio_info'), d.audio.split(';').map((s) => _T(s.split(': ')[0], s.split(': ').length > 1 ? s.split(': ')[1] : "")).toList()),
  ]);
}

class _ProcessorTab extends StatelessWidget {
  final DiscoveredDevice d; const _ProcessorTab({required this.d});
  @override
  Widget build(BuildContext context) => ListView(padding: const EdgeInsets.all(16), children: [_Sec(Strings.get('soc_info'), [_T(Strings.get('soc_info'), d.soc), _T(Strings.get('core_count'), d.cpuCores)])]);
}

class _ScreenTab extends StatelessWidget {
  final DiscoveredDevice d; const _ScreenTab({required this.d});
  @override
  Widget build(BuildContext context) => ListView(padding: const EdgeInsets.all(16), children: [_Sec(Strings.get('screen'), [_T(Strings.get('screen_res'), d.screenRes), _T(Strings.get('screen_refresh'), d.screenRefresh), _T(Strings.get('screen_size'), d.screenSize), _T('Display ID', d.displayId)])]);
}

class _CameraTab extends StatelessWidget {
  final DiscoveredDevice d; const _CameraTab({required this.d});
  @override
  Widget build(BuildContext context) => ListView(padding: const EdgeInsets.all(16), children: [_Sec(Strings.get('tab_camera'), d.cameras.split(';').map((c) => _T(c.split(':')[0], c.split(':').last)).toList())]);
}

class _GraphicsTab extends StatelessWidget {
  final DiscoveredDevice d; const _GraphicsTab({required this.d});
  @override
  Widget build(BuildContext context) => ListView(padding: const EdgeInsets.all(16), children: [_Sec(Strings.get('gpu_info'), [_T('GPU Renderer', d.gpuRenderer), _T('GPU Vendor', d.gpuVendor), _T('OpenGL', d.opengl), _T('Vulkan', d.vulkan), _T('OpenCL', d.opencl)])]);
}

class _SensorsTab extends StatelessWidget {
  final DiscoveredDevice d; const _SensorsTab({required this.d});
  @override
  Widget build(BuildContext context) => ListView(padding: const EdgeInsets.all(16), children: [_Sec(Strings.get('tab_sensors'), d.sensors.split(';').map((s) => _T(s.split(': ')[0], s.split(': ').length > 1 ? s.split(': ')[1] : "")).toList())]);
}

class _PartitionsTab extends StatelessWidget {
  final DiscoveredDevice d; const _PartitionsTab({required this.d});
  @override
  Widget build(BuildContext context) => ListView(padding: const EdgeInsets.all(16), children: [_Sec(Strings.get('tab_partitions'), d.partitions.split(';').map((p) => _T(p.split(':')[0], p.split(':').last)).toList())]);
}

class _BatteryTab extends StatelessWidget {
  final DiscoveredDevice d; const _BatteryTab({required this.d});
  @override
  Widget build(BuildContext context) => ListView(padding: const EdgeInsets.all(16), children: [_Sec(Strings.get('battery'), [_T(Strings.get('battery_level'), d.batteryLevel), _T('Voltage', d.batteryVolt), _T('Temperature', d.batteryTemp)])]);
}

// --- Settings ---
class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});
  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  late TextEditingController _portCtrl;
  late TextEditingController _adbPortCtrl;

  @override
  void initState() {
    super.initState();
    _portCtrl = TextEditingController(text: portNotifier.value.toString());
    _adbPortCtrl = TextEditingController(text: adbPortNotifier.value.toString());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(Strings.get('settings'))),
      body: ListView(padding: const EdgeInsets.all(16), children: [
        _Sec(Strings.get('lang_settings'), [
          ValueListenableBuilder<String>(valueListenable: localeNotifier, builder: (c, l, _) => Column(children: [
            RadioListTile(title: const Text('中文 (简体)'), value: 'zh', groupValue: l, onChanged: (v) => localeNotifier.value = v!),
            RadioListTile(title: const Text('English'), value: 'en', groupValue: l, onChanged: (v) => localeNotifier.value = v!),
          ]))
        ]),
        _Sec(Strings.get('theme_settings'), [
          ValueListenableBuilder<ThemeMode>(valueListenable: themeNotifier, builder: (c, m, _) => Column(children: [
            RadioListTile(title: Text(Strings.get('light')), value: ThemeMode.light, groupValue: m, onChanged: (v) => themeNotifier.value = v!),
            RadioListTile(title: Text(Strings.get('dark')), value: ThemeMode.dark, groupValue: m, onChanged: (v) => themeNotifier.value = v!),
            RadioListTile(title: Text(Strings.get('system_theme')), value: ThemeMode.system, groupValue: m, onChanged: (v) => themeNotifier.value = v!),
          ]))
        ]),
        _Sec("网络设置", [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(children: [
              TextField(
                controller: _portCtrl,
                decoration: InputDecoration(labelText: Strings.get('port'), border: const OutlineInputBorder()),
                keyboardType: TextInputType.number,
                onChanged: (v) => portNotifier.value = int.tryParse(v) ?? portNotifier.value,
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _adbPortCtrl,
                decoration: InputDecoration(labelText: Strings.get('adb_port'), border: const OutlineInputBorder()),
                keyboardType: TextInputType.number,
                onChanged: (v) => adbPortNotifier.value = int.tryParse(v) ?? adbPortNotifier.value,
              ),
            ]),
          ),
        ]),
      ]),
    );
  }
  @override
  void dispose() { _portCtrl.dispose(); _adbPortCtrl.dispose(); super.dispose(); }
}

// --- Common Widgets ---
class _Header extends StatelessWidget {
  final DiscoveredDevice d; const _Header({required this.d});
  @override
  Widget build(BuildContext context) {
    double lvl = double.tryParse(d.batteryLevel.replaceAll('%','')) ?? 0;
    return Card(color: Theme.of(context).colorScheme.primaryContainer.withOpacity(0.5), child: Stack(children: [
      FractionallySizedBox(widthFactor: lvl/100, child: Container(height: 160, decoration: BoxDecoration(color: Colors.green.withOpacity(0.2), borderRadius: BorderRadius.circular(12)))),
      Padding(padding: const EdgeInsets.all(20), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [const Icon(Icons.devices, size: 40), const SizedBox(width: 12), Expanded(child: Text(d.model, style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold), overflow: TextOverflow.ellipsis))]),
        const SizedBox(height: 12), Text('${Strings.get('manufacturer')}: ${d.manufacturer}', style: const TextStyle(fontSize: 16)),
        const SizedBox(height: 24), Row(mainAxisAlignment: MainAxisAlignment.end, children: [Text('${Strings.get('battery_level')}: ${d.batteryLevel}', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18))]),
      ]))
    ]));
  }
}

class _Stat extends StatelessWidget {
  final String t, v; final IconData i; final Color c; const _Stat(this.t, this.v, this.i, this.c);
  @override
  Widget build(BuildContext context) {
    return Card(child: Padding(padding: const EdgeInsets.all(8), child: Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
      Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [Expanded(child: Text(t, style: const TextStyle(fontSize: 9, color: Colors.grey), overflow: TextOverflow.ellipsis)), Icon(i, size: 12, color: c)]),
      Text(v, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 11), overflow: TextOverflow.ellipsis),
    ])));
  }
}

class _Usage extends StatelessWidget {
  final String t, tot, u; final Color c; const _Usage(this.t, this.tot, this.u, this.c);
  @override
  Widget build(BuildContext context) {
    double r = (double.tryParse(u.split(' ')[0]) ?? 0) / (double.tryParse(tot.split(' ')[0]) ?? 1);
    return Card(child: Padding(padding: const EdgeInsets.all(12), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [Text(t, style: const TextStyle(fontSize: 12, color: Colors.grey)), Text(tot, style: TextStyle(color: c, fontWeight: FontWeight.bold))]),
      const SizedBox(height: 8), LinearProgressIndicator(value: r.clamp(0, 1), color: c, backgroundColor: c.withOpacity(0.1)),
      const SizedBox(height: 4), Text('${Strings.get('used')} $u', style: const TextStyle(fontSize: 10, color: Colors.grey)),
    ])));
  }
}

class _Sec extends StatelessWidget {
  final String t; final List<Widget> c; const _Sec(this.t, this.c);
  @override
  Widget build(BuildContext context) {
    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Padding(padding: const EdgeInsets.only(left: 8, bottom: 4, top: 12), child: Text(t, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.blue, fontSize: 12))),
      Card(child: Column(children: c)),
    ]);
  }
}

class _T extends StatelessWidget {
  final String l, v; const _T(this.l, this.v);
  @override
  Widget build(BuildContext context) {
    return ListTile(title: Text(l, style: const TextStyle(fontSize: 11, color: Colors.grey)), trailing: Text(v, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold)), dense: true, visualDensity: VisualDensity.compact);
  }
}
