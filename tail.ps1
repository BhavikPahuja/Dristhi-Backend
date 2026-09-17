param([int]$Lines = 25)
$log = 'd:\Tekathon\Drishti\build.log'
if (-not (Test-Path $log)) { Write-Output 'NO LOG YET'; exit }
$c = Get-Content $log
$n = $c.Count
$start = [Math]::Max(0, $n - $Lines)
$c[$start..($n-1)]