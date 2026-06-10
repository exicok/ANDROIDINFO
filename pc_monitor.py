import socket
import tkinter as tk
from tkinter import ttk, messagebox
import threading
import time

class PCMonitor:
    def __init__(self, root):
        self.root = root
        self.root.title("Android 设备高级监控端 (PC)")
        self.root.geometry("900x550")

        self.port = 8888
        self.devices = {}  # {id: info_dict}
        self.search_query = ""

        self.setup_ui()

        self.running = True
        self.socket = None
        self.start_listening()

        # 定时器：每秒刷新一次列表（处理倒计时或状态）
        self.tick()

    def setup_ui(self):
        # --- 顶部控制栏 ---
        top_frame = tk.Frame(self.root, pady=10, padx=10)
        top_frame.pack(fill='x')

        tk.Label(top_frame, text="搜索设备:").pack(side='left')
        self.search_entry = tk.Entry(top_frame, width=20)
        self.search_entry.pack(side='left', padx=5)
        self.search_entry.bind("<KeyRelease>", self.on_search)

        tk.Label(top_frame, text="监听端口:").pack(side='left', padx=(20, 0))
        self.port_entry = tk.Entry(top_frame, width=8)
        self.port_entry.insert(0, "8888")
        self.port_entry.pack(side='left', padx=5)

        tk.Button(top_frame, text="应用并重启", command=self.restart_listening).pack(side='left')

        self.status_label = tk.Label(top_frame, text="● 正在监听", fg="green")
        self.status_label.pack(side='right')

        # --- 设备列表 ---
        list_frame = tk.Frame(self.root, padx=10, pady=5)
        list_frame.pack(fill='both', expand=True)

        columns = ('id', 'model', 'brand', 'android', 'ip', 'last_seen')
        self.tree = ttk.Treeview(list_frame, columns=columns, show='headings')

        self.tree.heading('id', text='设备 ID')
        self.tree.heading('model', text='型号')
        self.tree.heading('brand', text='制造商')
        self.tree.heading('android', text='Android 版本')
        self.tree.heading('ip', text='来源 IP')
        self.tree.heading('last_seen', text='最后通信时间')

        for col in columns:
            self.tree.column(col, width=120, anchor='center')

        self.tree.pack(side='left', fill='both', expand=True)

        scrollbar = ttk.Scrollbar(list_frame, orient="vertical", command=self.tree.yview)
        scrollbar.pack(side='right', fill='y')
        self.tree.configure(yscrollcommand=scrollbar.set)

        # 绑定点击事件
        self.tree.bind("<Double-1>", self.on_item_double_click)

        # 底部提示
        tk.Label(self.root, text="提示: 双击列表中的设备查看详细硬件信息", fg="gray", pady=5).pack()

    def on_search(self, event):
        self.search_query = self.search_entry.get().lower()
        self.refresh_ui()

    def on_item_double_click(self, event):
        item_id = self.tree.focus()
        if not item_id: return

        values = self.tree.item(item_id)['values']
        device_id = str(values[0])
        if device_id in self.devices:
            self.show_details(self.devices[device_id])

    def show_details(self, dev):
        details_win = tk.Toplevel(self.root)
        details_win.title(f"设备详情 - {dev['model']}")
        details_win.geometry("450x400")
        details_win.resizable(False, False)

        main_frame = tk.Frame(details_win, padx=20, pady=20)
        main_frame.pack(fill='both', expand=True)

        tk.Label(main_frame, text="详细硬件参数", font=('Arial', 14, 'bold')).pack(pady=(0, 15))

        # 详细信息字段 (基于 Android 端发送的新协议)
        # 格式: ID|MODEL|MANUFACTURER|VERSION|CPU|MEM|BATTERY|TIME
        info_map = [
            ("设备 ID", dev.get('id')),
            ("型号", dev.get('model')),
            ("品牌", dev.get('brand')),
            ("Android 版本", dev.get('android')),
            ("处理器 (SoC)", dev.get('cpu', '未知')),
            ("内存总量", dev.get('mem', '未知')),
            ("当前电量", dev.get('battery', '未知')),
            ("数据更新时间", dev.get('time', '未知')),
            ("来源 IP 地址", dev.get('ip'))
        ]

        for label, val in info_map:
            row = tk.Frame(main_frame, pady=5)
            row.pack(fill='x')
            tk.Label(row, text=f"{label}:", width=15, anchor='w', fg="gray").pack(side='left')
            tk.Label(row, text=val, fontWeight='bold', anchor='w').pack(side='left')

        tk.Button(details_win, text="关闭", command=details_win.destroy, width=10).pack(pady=10)

    def udp_listener(self):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        try:
            self.socket.bind(('', self.port))
        except Exception as e:
            self.root.after(0, lambda: self.status_label.config(text=f"● 绑定失败: {e}", fg="red"))
            return

        while self.running:
            try:
                self.socket.settimeout(1.0)
                data, addr = self.socket.recvfrom(2048)
                msg = data.decode('utf-8')
                parts = msg.split('|')

                # 兼容旧版(4个字段)和新版(8个字段)
                if len(parts) >= 4:
                    dev_id = parts[0]
                    dev_data = {
                        'id': dev_id,
                        'model': parts[1],
                        'brand': parts[2],
                        'android': parts[3],
                        'ip': addr[0],
                        'raw_time': time.time()
                    }

                    if len(parts) >= 8:
                        dev_data.update({
                            'cpu': parts[4],
                            'mem': parts[5],
                            'battery': parts[6],
                            'time': parts[7]
                        })

                    self.devices[dev_id] = dev_data
                    self.root.after(0, self.refresh_ui)
            except (socket.timeout, OSError):
                continue
            except Exception as e:
                print(f"UDP Error: {e}")

    def refresh_ui(self):
        # 记住当前选中的项
        selected = self.tree.selection()

        # 清空
        for item in self.tree.get_children():
            self.tree.delete(item)

        # 过滤并显示
        for dev_id, info in self.devices.items():
            # 搜索过滤
            search_text = f"{info['model']} {info['brand']} {info['id']} {info['ip']}".lower()
            if self.search_query and self.search_query not in search_text:
                continue

            last_seen_str = time.strftime("%H:%M:%S", time.localtime(info['raw_time']))
            self.tree.insert('', 'end', values=(
                dev_id,
                info['model'],
                info['brand'],
                info['android'],
                info['ip'],
                last_seen_str
            ))

    def tick(self):
        # 每 10 秒清理一次离线设备
        now = time.time()
        stale = [k for k, v in self.devices.items() if now - v['raw_time'] > 15]
        if stale:
            for k in stale: del self.devices[k]
            self.refresh_ui()

        self.root.after(5000, self.tick)

    def restart_listening(self):
        try:
            new_port = int(self.port_entry.get())
            self.running = False
            if self.socket: self.socket.close()
            self.port = new_port
            self.devices.clear()
            self.refresh_ui()
            self.running = True
            threading.Thread(target=self.udp_listener, daemon=True).start()
            self.status_label.config(text="● 正在监听", fg="green")
        except ValueError:
            messagebox.showerror("错误", "请输入有效的端口号")

    def start_listening(self):
        threading.Thread(target=self.udp_listener, daemon=True).start()

if __name__ == "__main__":
    root = tk.Tk()
    # 尝试设置现代风格
    style = ttk.Style()
    if 'clam' in style.theme_names():
        style.theme_use('clam')

    app = PCMonitor(root)
    root.mainloop()
