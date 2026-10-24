param([string]$File = "$PSScriptRoot\beispiel-falscher-svtcode-pfad.log")
$ErrorActionPreference="Stop"
$full=(Resolve-Path $File).Path
$raw=[System.IO.File]::ReadAllText($full,[System.Text.Encoding]::UTF8)
$body=@{caseText=$raw;question="Analysiere Ursache und minimalen XPath-Fix. Prüfe den Vorschlag lokal und antworte auf Deutsch."} | ConvertTo-Json -Depth 6
$result=Invoke-RestMethod -Uri "http://127.0.0.1:8091/api/v1/diagnostics/analyze" -Method Post -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
$result.analysis
"Durch AGATE verifiziert: $($result.verified) ($($result.verificationStatus))"
