# Ptero-PlaceholderExpansion

A plugin to retrieve Pterodactyl server information using PlaceholderAPI

## How to use placeholders

You can use the following placeholders to retrieve Pterodactyl server information.

```
%ptero_[serverID]_[JSONpath]%
```

## Sample

![Screenshot1](assets/screenshot_1.png)
```
"%ptero_9be90f7c_name% server (%ptero_9be90f7c_description%)"
"Max RAM: %ptero_9be90f7c_limits.memory% MiB"
"Version: %ptero_9be90f7c_relationships.variables.data[0].attributes.server_value%"
"Status: %ptero_9be90f7c_current_state%"
```

### The above example will be as follows if the Pterodactyl API response is as below
To display the memory of the attributes' limits, it will be `limits.memory`

```json
{
    "attributes": {
        "server_owner": true,
        "identifier": "9be90f7c",
        "internal_id": 18,
        "uuid": "56d995cd-8195-4f74-a382-0b90786c0f54",
        "name": "test",
        "node": "rack-server",
        "is_node_under_maintenance": false,
        "sftp_details": {
            "ip": "mc.kairun.jp",
            "port": 2023
        },
        "description": "テストサーバーです", // EN: This is a test server
        "limits": {
            "memory": 4096,
            "swap": 16384,
            "disk": 2048,
            "io": 500,
            "cpu": 0,
            "threads": null,
            "oom_disabled": true
        },
        "current_state": "offline",
        "is_suspended": false,
        "resources": {
          "memory_bytes": 0,
          "cpu_absolute": 0,
....
```

### Installation

1. If you haven't installed PlaceholderAPI, please do so.
2. Download the latest jar file from the release page.
3. Place the jar file in the `plugins/PlaceholderAPI/expansions` folder.
4. Reload the expansion with the `/papi reload` command.
5. Add the following settings to the `plugins/PlaceholderAPI/config.yml` file.

    ```yaml
      ptero:
        pterodactyl:
        url: https://panel.example.com # Pterodactyl URL
        apiKey: ptlc_XXXXXXXXXXXXXXXX # Pterodactyl API key
        cacheTime: 60
        error_msg: ERROR
        custom_headers: # Custom headers (if needed)
          CF-Access-Client-Id: XXXXX.access # Example: Cloudflare Access Client ID
          CF-Access-Client-Secret: XXXXXX # Example: Cloudflare Access Client Secret
    ```


### Special Thanks
- Kamesuta