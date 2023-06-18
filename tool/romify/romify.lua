require "lfs"

function dirtree(dir)
    assert(dir and dir ~= "", "Please pass directory parameter")
    if string.sub(dir, -1) == "/" then
        dir=string.sub(dir, 1, -2)
    end

    local function yieldtree(dir)
        for entry in lfs.dir(dir) do
            if entry ~= "." and entry ~= ".." then
                entry=dir.."/"..entry
                local attr=lfs.attributes(entry)
                coroutine.yield(entry,attr)
                if attr.mode == "directory" then
                    yieldtree(entry)
                end
            end
        end
    end

    return coroutine.wrap(function() yieldtree(dir) end)
end

function string.startsWith(String,Start)
   return string.sub(String,1,string.len(Start))==Start
end

function dump(o)
   if type(o) == 'table' then
      local s = '{ '
      for k,v in pairs(o) do
         -- special case to prevent infinite recursion in curpath tree
         if k ~= "parent" then
            if type(k) ~= 'number' then k = '"'..k..'"' end
            s = s .. '['..k..'] = ' .. dump(v) .. ','
         end
      end
      return s .. '} '
   else
      return tostring(o)
   end
end

function output(node)
   if node.type == "file" then
      print("", "file", node.name, "["..string.len(node.contents).."]")
   else
      print("", "directory", node.name)
      for k,v in pairs(node.contents) do
        print(k)
        output(v)
      end
   end
end

searchpath = "../../build/im4000/class"
tree = {}
curdir = { name = "", type = "directory", contents = {}}
rootdir = curdir

print("create hastable:ROMFS/")
for filename, attr in dirtree(searchpath) do
    relpath = string.sub(filename, string.len(searchpath) + 2)
    if attr.mode == "directory" then
      print("create hastable:ROMFS/" .. relpath)
      table.insert(tree, {relpath=relpath, type="directory"})
      while (curdir) do
        curpath = curdir.relpath
        if curpath == nil or string.startsWith(relpath, curpath.."/") then
          subpath = string.sub(relpath, curpath and (string.len(curpath) + 2) or 1)
          print("add hashtable:ROMFS/"..relpath.." to hastable:ROMFS/"..(curpath and curpath or "").." at "..subpath)
          newdir = { relpath = relpath, name = subpath, type = "directory", contents = {} , parent = curdir}
          curdir.contents[subpath] =  newdir
          curdir = newdir
          break
        else
          curdir = curdir.parent
        end
      end
    else
      while (curdir) do
        curpath = curdir.relpath
        if curpath == nil or string.startsWith(relpath, curpath.."/") then
          subpath = string.sub(relpath, curpath and (string.len(curpath) + 2) or 1)
          print("add file "..relpath.." to hastable:ROMFS/"..(curpath and curpath or "").." at "..subpath)
          file = assert(io.open(filename, "rb"))
          print("    file " .. relpath .. " has length " .. lfs.attributes (filename, "size"))
          contents = file:read("*a")
          file.close()
          table.insert(tree, {relpath=relpath, type="file", contents=string.format("%d bytes", string.len(contents))})
          curdir.contents[subpath] =  { name = subpath, type = "file", contents = contents }
          break
        else
          curdir = curdir.parent
        end
      end
    end
end

--print(dump(tree))
--output(rootdir)
