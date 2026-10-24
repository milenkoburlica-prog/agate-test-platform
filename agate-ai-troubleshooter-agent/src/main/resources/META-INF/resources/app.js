"use strict";
const $ = id => document.getElementById(id);
const MAX_LOG_BYTES=5*1024*1024;
const logBytes=text=>new Blob([text]).size;
const sessions = new Map(); let currentId=null, fileText='', fileName='', demoCache=null, busy=false;
const id=()=>('local-'+crypto.randomUUID());
function newSession(){const s={id:id(),serverSessionId:null,messages:[],caseText:'',title:'Neuer Chat'};sessions.set(s.id,s);currentId=s.id;render();return s;}
function current(){return sessions.get(currentId);}
function renderSessions(){const nav=$('sessions');nav.replaceChildren();[...sessions.values()].reverse().forEach(s=>{const b=document.createElement('button');b.className='chat-entry'+(s.id===currentId?' active':'');b.textContent=s.title;b.title=s.title;b.onclick=()=>{if(busy)return;currentId=s.id;fileText='';fileName='';updateAttachment();render();closeSidebar();};nav.append(b);});}
function createText(tag,cls,text){const e=document.createElement(tag);if(cls)e.className=cls;e.textContent=text;return e;}
function showWelcome(){const e=createText('section','welcome','');e.append(createText('div','welcome-symbol','◈'),createText('h1','', 'Wie kann ich dir helfen?'),createText('p','', 'Ich analysiere AGATE-Fehler in SOAP, SQL, CMD, OpenShift, REST, GUI und BUFFER anhand deiner Logs.'));const sug=createText('div','suggestions','');[['Was kannst du?', 'Was kannst du eigentlich? Welche AGATE-Tools stehen dir zur Verfügung?'],['Testfehler analysieren','Analysiere den Fehler und schlage eine überprüfbare Korrektur vor.'],['Test erstellen?', 'Kannst du mir einen neuen AGATE-Test erstellen?']].forEach(([label,msg])=>{const b=createText('button','',label);b.type='button';b.onclick=()=>{$('message').value=msg;resize();$('message').focus();};sug.append(b)});e.append(sug);$('conversation').append(e);}
function showLog(s, ref){
  const log=s.caseText||'';
  if(!log){info('Originalprotokoll','Für diese Nachricht ist kein Protokoll verfügbar.');return;}
  const lines=log.split(/\r?\n/);
  const dialog=$('logDialog'), container=$('logLines'); container.replaceChildren();
  $('logTitle').textContent=s.logName||'Originalprotokoll';
  const from=ref?Math.max(1,ref.startLine-6):1;
  const to=ref?Math.min(lines.length,ref.endLine+9):Math.min(lines.length,1200);
  lines.slice(from-1,to).forEach((line,i)=>{
    const n=i+from,row=createText('div','log-line','');row.dataset.line=n;
    const relevant=ref && n>=ref.startLine && n<=ref.endLine;
    if(relevant)row.classList.add('log-focus');
    if(/STEP FINISHED.*FAILED|STEP FAILED|ERROR:/.test(line))row.classList.add('log-error');
    if(/SKIPPED \(Condition false/.test(line))row.classList.add('log-skipped');
    row.append(createText('span','log-number',String(n)),createText('span','log-text',line));container.append(row);
  });
  dialog.showModal();
  requestAnimationFrame(()=>{const target=container.querySelector('[data-line="'+from+'"]');if(target)target.scrollIntoView({block:'start'});});
  $('logRange').textContent=ref?'Referenz: Zeilen '+ref.startLine+'–'+ref.endLine:'Gesamtes Protokoll · '+lines.length+' Zeilen';
}
function renderDiagnostic(body,turn,s){
 const d=turn.diagnostic;
 const section=createText('section','diagnostic-view','');
 section.append(createText('div','diagnostic-kicker','◈ AGATE AI · Fehleranalyse'),createText('h3','diagnostic-title',d.title||'Diagnose'));
 section.append(createText('p','diagnostic-summary',d.summary||''));
 if(d.expected!==null && d.expected!==undefined && d.actual!==null && d.actual!==undefined){
   const comparison=createText('div','value-comparison','');
   const expected=createText('div','compare-value','');expected.append(createText('strong','compare-label','ERWARTET'),createText('p','',d.expected));
   const actual=createText('div','compare-value','');actual.append(createText('strong','compare-label','TATSÄCHLICH'),createText('p','',d.actual));
   comparison.append(expected,actual);section.append(comparison);
 }
 const fix=createText('div','fix-panel','');fix.append(createText('h4','','Vorgeschlagene Korrektur'),createText('pre','fix-content',d.fix||'Keine Korrektur verfügbar.'));section.append(fix);
 const state=createText('div','diagnostic-verification','◉ '+(d.verification||turn.status||'Nicht verifiziert'));section.append(state);
 const steps=d.steps||[];
 if(steps.length){const panel=createText('section','step-panel','');panel.append(createText('h4','','Relevante Testschritte'));
  steps.forEach((ref,i)=>{const btn=createText('button','step-card','');btn.type='button';btn.append(createText('span','step-index',String(i+1)),createText('span','step-title',(ref.label||'Testschritt')+(ref.response?' · '+ref.response:'')),createText('span','step-state '+(ref.status||'').toLowerCase(),ref.status||'UNKNOWN'),createText('span','step-range','Zeilen '+ref.startLine+'–'+ref.endLine+' ↗'));btn.onclick=()=>showLog(s,ref);panel.append(btn);});section.append(panel);
 }
 const bar=createText('div','evidence-actions','');const logButton=createText('button','evidence-button','▤ Originalprotokoll anzeigen');logButton.onclick=()=>showLog(s,null);bar.append(logButton);
 const details=document.createElement('details');details.className='tech-details';details.append(createText('summary','','Vollständige technische Analyse'));
 const pre=createText('pre','raw-analysis',turn.text);details.append(pre);bar.append(details);section.append(bar);
 body.append(section);
}
function appendTurn(turn){const row=createText('div','turn '+turn.role,'');if(turn.role==='user'){const b=createText('div','user-bubble',turn.text);if(turn.file)b.append(createText('div','message-attachment','▤ '+turn.file));row.append(b);}else{row.append(createText('div','ai-avatar','◈'));const body=createText('div','ai-content','');const s=current();if(turn.diagnostic){renderDiagnostic(body,turn,s)}else{const parts=turn.text.split(/(```[\s\S]*?```)/g);parts.forEach(part=>{if(!part)return;if(part.startsWith('```')){const pre=document.createElement('pre');pre.append(createText('code','',part.replace(/^```[^\n]*\n?/,'').replace(/```$/,'')));body.append(pre)}else{body.append(createText('p','',part))}});if(turn.status){const ver=createText('div','verification '+(turn.status==='XPATH_LOKAL_BESTÄTIGT'?'good':turn.status.includes('FEHL')?'bad':''),'◉ '+(turn.status==='XPATH_LOKAL_BESTÄTIGT'?'XPath lokal bestätigt':turn.status));body.append(ver);}}row.append(body);}$('conversation').append(row);}
function render(){renderSessions();const view=$('conversation');view.replaceChildren();const s=current();if(!s||!s.messages.length)showWelcome();else s.messages.forEach(appendTurn);scrollBottom();}
function scrollBottom(){requestAnimationFrame(()=>{const main=document.querySelector('.main');main.scrollTop=main.scrollHeight;});}
function resize(){const m=$('message');m.style.height='auto';m.style.height=Math.min(m.scrollHeight,180)+'px';}
function updateAttachment(){$('attachment').hidden=!fileName;$('attachmentName').textContent=fileName;$('attachmentSize').textContent=fileName?fileText.split(/\r?\n/).length+' Zeilen · '+(new Blob([fileText]).size/1024).toFixed(1)+' KB · wird mit der nächsten Nachricht gesendet':'';}
function closeSidebar(){document.querySelector('.layout').classList.remove('nav-open')}
function setBusy(value){busy=value;$('send').disabled=value;$('message').disabled=value;$('newChat').disabled=value;$('newChatTop').disabled=value;$('status').textContent=value?'Analysiere …':'Bereit';}
async function attach(f){if(!f)return;const text=await f.text();if(logBytes(text)>MAX_LOG_BYTES){alert('Das Protokoll ist zu groß (maximal 5 MB).');return;}fileText=text;fileName=f.name;updateAttachment();}
async function loadDemo(){try{if(demoCache===null){const r=await fetch('/demo.log');if(!r.ok)throw new Error('Demofall nicht verfügbar');demoCache=await r.text();}fileText=demoCache;fileName='demo.log';updateAttachment();$('message').value='Analysiere diesen fehlgeschlagenen AGATE-Test und überprüfe die XPath-Korrektur.';resize();$('message').focus();}catch(e){alert(e.message)}}
async function send(e){e.preventDefault();if(busy)return;const message=$('message').value.trim() || 'Analysiere dieses AGATE-Protokoll und schlage eine überprüfbare Korrektur vor.';if(!message&&!fileText)return;const s=current(),log=fileText;const file=fileName;s.messages.push({role:'user',text:message,file});if(s.messages.filter(x=>x.role==='user').length===1)s.title=message.length>39?message.substring(0,39)+'…':message;if(log){s.caseText=log;s.logName=file;}fileText='';fileName='';$('file').value='';updateAttachment();$('message').value='';resize();render();setBusy(true);try{const response=await fetch('/api/v1/chat',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({sessionId:s.serverSessionId,message,caseText:log||''})});let data;try{data=await response.json()}catch{throw new Error('Serverantwort ist kein gültiges JSON.')}if(!response.ok)throw new Error(data.error||'HTTP '+response.status);s.serverSessionId=data.sessionId;s.messages.push({role:'assistant',text:data.answer||'Keine Antwort.',intent:data.intent,status:data.verificationStatus,diagnostic:data.diagnostic});}catch(err){s.messages.push({role:'assistant',text:'Die Anfrage konnte nicht verarbeitet werden: '+err.message});}finally{setBusy(false);render();$('message').focus();}}
function info(title,content){$('dialogTitle').textContent=title;const b=$('dialogBody');b.replaceChildren();if(typeof content==='string')b.append(createText('p','',content));else b.append(content);$('infoDialog').showModal();}
function looksLikeAgateLog(text){
  return text.length>=180 && (/>>>\s*DSL\s+-\s*type:|(?:SOAP|SQL|REST|GUI|CMD|BUFFER) STEP FINISHED|TEST RESULT:|AGATE PROJECT|SQL response was not found/i.test(text)) && text.includes('\n');
}
function attachPastedLog(text){
  if(logBytes(text)>MAX_LOG_BYTES){alert('Das Protokoll ist zu groß (maximal 5 MB).');return;}
  fileText=text;fileName='Eingefügtes AGATE-Protokoll';updateAttachment();$('message').focus();
}
$('message').addEventListener('paste',event=>{
  const data=event.clipboardData;
  if(!data)return;
  const text=data.getData('text/plain');
  if(!looksLikeAgateLog(text))return;
  event.preventDefault();
  if(fileText && !confirm('Vorhandenes Protokoll durch den eingefügten Text ersetzen?'))return;
  attachPastedLog(text);
});
$('pasteLog').onclick=()=>{
  const dlg=$('pasteDialog');$('manualLog').value='';dlg.showModal();$('manualLog').focus();
};
$('cancelPaste').onclick=()=>$('pasteDialog').close();
$('acceptPaste').onclick=()=>{
  const text=$('manualLog').value;
  if(!text.trim()){alert('Bitte ein Protokoll einfügen.');return;}
  if(logBytes(text)>MAX_LOG_BYTES){alert('Das Protokoll ist zu groß (maximal 5 MB).');return;}
  attachPastedLog(text);$('pasteDialog').close();
};
$('form').addEventListener('submit',send);$('message').addEventListener('input',resize);$('message').addEventListener('keydown',e=>{if(e.key==='Enter'&&!e.shiftKey&&!e.isComposing){e.preventDefault();$('form').requestSubmit();}});
$('attachButton').onclick=()=>$('file').click();$('file').onchange=e=>attach(e.target.files[0]);$('removeFile').onclick=()=>{fileText='';fileName='';$('file').value='';updateAttachment()};$('demo').onclick=loadDemo;
function start(){if(busy)return;newSession();fileText='';fileName='';updateAttachment();closeSidebar();$('message').focus()};$('newChat').onclick=start;$('newChatTop').onclick=start;
$('openSidebar').onclick=()=>document.querySelector('.layout').classList.add('nav-open');$('closeSidebar').onclick=closeSidebar;$('backdrop').onclick=closeSidebar;
$('closeDialog').onclick=()=>$('infoDialog').close();$('openHelp').onclick=()=>info('Datenschutz & Sicherheit','Dateien werden im Browser gelesen und an den lokalen Quarkus-Server gesendet. Bei einer SOAP-Diagnose können die Inhalte anschließend an Groq Cloud übertragen werden. Andere Loganalysen laufen lokal. Bitte ausschließlich synthetische oder ausdrücklich freigegebene Daten verwenden. Der Chatverlauf wird nur im Speicher dieser Browserseite gehalten und nach dem Neuladen gelöscht. Es werden keine Tests, SQL- oder OpenShift-Kommandos ausgeführt.');
$('showCapabilities').onclick=async()=>{try{const r=await fetch('/api/v1/chat/capabilities');if(!r.ok)throw Error('HTTP '+r.status);const items=await r.json();const ul=document.createElement('ul');items.forEach(t=>{ul.append(createText('li','', (t.available?'✓ ':'○ ')+t.name+' – '+t.description))});info('AGATE Tools & Funktionen',ul);}catch(e){info('AGATE Tools','Funktionen konnten nicht geladen werden: '+e.message)}closeSidebar();};
document.addEventListener('keydown',e=>{if(e.altKey&&e.key.toLowerCase()==='n'){e.preventDefault();start()}if(e.key==='Escape')closeSidebar()});newSession();resize();
$('closeLog').onclick=()=>$('logDialog').close();
$('logSearch').addEventListener('input',e=>{const term=e.target.value.toLowerCase();for(const row of $('logLines').children){row.classList.toggle('log-search-match',!!term&&row.textContent.toLowerCase().includes(term));}const first=$('logLines').querySelector('.log-search-match');if(first)first.scrollIntoView({block:'center'});});
