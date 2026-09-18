const $=s=>document.querySelector(s);let projects=[];const dialog=$('#projectDialog');
async function load(){projects=await (await fetch('/api/projects')).json();render();const cats=[...new Set(projects.map(p=>p.category))];$('#category').innerHTML='<option value="">ALL CATEGORIES</option>'+cats.map(c=>`<option>${c}</option>`).join('');$('#count').textContent=String(projects.length).padStart(2,'0');$('#statProjects').textContent=String(projects.length).padStart(2,'0');$('#statCategories').textContent=cats.length;$('#statTech').textContent=new Set(projects.flatMap(p=>p.technology.split('·').map(x=>x.trim()))).size}
function render(){const q=$('#search').value.toLowerCase(),c=$('#category').value;const list=projects.filter(p=>(!q||JSON.stringify(p).toLowerCase().includes(q))&&(!c||p.category===c));$('#projectList').innerHTML=list.map((p,i)=>`<article class="project-row" data-id="${p.id}"><span class="num">${String(i+1).padStart(2,'0')}</span><span class="title">${p.title}</span><span class="cat">${p.category}</span><span class="date">${p.createdAt}</span><span class="arrow">↗</span></article>`).join('')||'<div style="padding:30px 0;font-family:var(--mono)">NO PROJECTS FOUND.</div>';document.querySelectorAll('.project-row').forEach(r=>r.onclick=()=>openProject(+r.dataset.id));const p=projects[0];if(p)$('#featured').innerHTML=`<div class="image" style="background-image:url('${p.image}')"></div><div class="copy"><span class="tag">FEATURED / 01</span><h3>${p.title}</h3><p>${p.description}</p><span class="eyebrow">${p.technology}</span></div>`}
function fill(p={}){[['projectId','id'],['fTitle','title'],['fCategory','category'],['fDescription','description'],['fProblem','problem'],['fSolution','solution'],['fTechnology','technology'],['fImage','image']].forEach(([a,b])=>$('#'+a).value=p[b]||'')}
function openProject(id){fill(projects.find(p=>p.id===id));$('#deleteBtn').style.display='block';dialog.showModal()}$('#newBtn').onclick=()=>{fill();$('#deleteBtn').style.display='none';dialog.showModal()};$('#projectForm').onsubmit=async e=>{e.preventDefault();const id=$('#projectId').value;const body={title:$('#fTitle').value,category:$('#fCategory').value,description:$('#fDescription').value,problem:$('#fProblem').value,solution:$('#fSolution').value,technology:$('#fTechnology').value,image:$('#fImage').value,githubUrl:'https://github.com',demoUrl:'https://example.com'};await fetch(id?`/api/projects/${id}`:'/api/projects',{method:id?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});dialog.close();load()};$('#deleteBtn').onclick=async()=>{const id=$('#projectId').value;if(id&&confirm('Delete this project from the archive?')){await fetch('/api/projects/'+id,{method:'DELETE'});dialog.close();load()}};$('#search').oninput=render;$('#category').onchange=render;$('#closeBtn').onclick=()=>dialog.close();dialog.onclick=e=>{if(e.target===dialog)dialog.close()};load();

// Mobile menu toggle
const hamburgerBtn = $('#hamburgerBtn');
const mobileNav = $('#mobileNav');
const mobileLinks = document.querySelectorAll('.mobile-link');

if (hamburgerBtn && mobileNav) {
    hamburgerBtn.onclick = () => {
        hamburgerBtn.classList.toggle('open');
        mobileNav.classList.toggle('open');
    };
    
    mobileLinks.forEach(link => {
        link.onclick = () => {
            hamburgerBtn.classList.remove('open');
            mobileNav.classList.remove('open');
        };
    });
}
