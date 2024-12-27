# Ptero-PlaceholderExpansion

## Pterodactylのサーバー情報をPlaceholderAPIで取得するプラグイン
### プレースホルダーの使用方法

以下のプレースホルダーを使用して、Pterodactylのサーバー情報を取得できます。

```
%ptero_サーバーID_変数%
```

#### 例\
```json
{
    "attributes": {
        "server_owner": true,
        "identifier": "f684cac7",
        "internal_id": 18,
        "uuid": "f684cac7-d9cb-414f-b9a4-70ed28cc9da6",
        "name": "hub",
        "node": "rack-server",
        "is_node_under_maintenance": false,
        "sftp_details": {
            "ip": "mc.kairun.jp",
            "port": 2023
        },
        "description": "異論なサーバーをつなげるサーバー",
        "limits": {
            "memory": 4096,
            "swap": 16384,
            "disk": 2048,
            "io": 500,
            "cpu": 0,
            "threads": null,
            "oom_disabled": true
        },
        ....
```

```
"%ptero_f684cac7_name%鯖 (%ptero_f684cac7_description%)"
"%ptero_f684cac7_limits.memory% MiB"
```
