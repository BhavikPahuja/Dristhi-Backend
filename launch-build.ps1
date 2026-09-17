Push-Location 'd:\Tekathon\Drishti'
if (Test-Path 'build.log') { Remove-Item 'build.log' -Force }
Start-Process -WindowStyle Hidden -FilePath 'cmd.exe' -ArgumentList '/c', 'run-build.cmd'
Pop-Location
Write-Output 'LAUNCHED-FROM-PS1'