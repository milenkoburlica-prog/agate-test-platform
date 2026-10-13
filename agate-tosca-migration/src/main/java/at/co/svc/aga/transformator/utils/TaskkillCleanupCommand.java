package at.co.svc.aga.transformator.utils;

/** Recognizes the existing browser-cleanup contract, without changing other CMD steps. */
public final class TaskkillCleanupCommand {
    private TaskkillCleanupCommand() {}
    public static String rewrite(String command) {
        String compact=command.trim().replaceAll("\\s+"," ");
        String known="\"{B[B_Browser_Path_Del]}\" {B[B_Browser_Arguments1_Del]} {B[B_Browser_Arguments2_Del]} {B[B_Browser_Arguments3_Del]} /T";
        if(!compact.equals(known))return command;
        return "powershell.exe -NoLogo -NoProfile -NonInteractive -Command \""
            + "$ErrorActionPreference='Stop'; "
            + "$exe='{B[B_Browser_Path_Del]}'; $mode='{B[B_Browser_Arguments1_Del]}'; "
            + "$image='{B[B_Browser_Arguments2_Del]}'; $force='{B[B_Browser_Arguments3_Del]}'; "
            + "if([IO.Path]::GetFileName($exe) -notin @('taskkill','taskkill.exe') -or $mode -ne '/im' -or $force -ne '/f' -or $image -notmatch '^[A-Za-z0-9_.-]+\\.exe$'){throw 'Invalid browser cleanup parameters'}; "
            + "$running=@(Get-CimInstance Win32_Process -ErrorAction Stop | Where-Object { $_.Name -ieq $image }); "
            + "if($running.Count -eq 0){Write-Output 'Browser process already absent'; exit 0}; "
            + "& $exe $mode $image $force '/T'; exit $LASTEXITCODE\"";
    }
}
