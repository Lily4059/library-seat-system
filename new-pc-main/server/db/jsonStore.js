import { readFileSync, writeFileSync, existsSync } from 'fs'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const dataDir = join(__dirname, '..', 'data')

class JsonStore {
  constructor(filename) {
    this.filePath = join(dataDir, filename)
    this.cache = null
    this.load()
  }

  load() {
    if (existsSync(this.filePath)) {
      const content = readFileSync(this.filePath, 'utf-8')
      this.cache = JSON.parse(content)
    } else {
      this.cache = {}
    }
  }

  save() {
    writeFileSync(this.filePath, JSON.stringify(this.cache, null, 2), 'utf-8')
  }

  getAll() {
    return this.cache
  }

  getCollection(collectionName) {
    if (!this.cache[collectionName]) {
      this.cache[collectionName] = []
    }
    return this.cache[collectionName]
  }

  findById(collectionName, id) {
    const collection = this.getCollection(collectionName)
    return collection.find(item => item.id === id)
  }

  findOne(collectionName, predicate) {
    const collection = this.getCollection(collectionName)
    if (typeof predicate === 'function') {
      return collection.find(predicate)
    }
    return collection.find(item => {
      for (const key in predicate) {
        if (item[key] !== predicate[key]) return false
      }
      return true
    })
  }

  findMany(collectionName, predicate) {
    const collection = this.getCollection(collectionName)
    if (typeof predicate === 'function') {
      return collection.filter(predicate)
    }
    return collection.filter(item => {
      for (const key in predicate) {
        if (item[key] !== predicate[key]) return false
      }
      return true
    })
  }

  insert(collectionName, item) {
    const collection = this.getCollection(collectionName)
    const nextIdKey = 'nextId'
    
    if (!this.cache[nextIdKey]) {
      this.cache[nextIdKey] = 1
    }
    
    const newItem = {
      ...item,
      id: item.id || this.cache[nextIdKey],
      createdAt: item.createdAt || new Date().toISOString()
    }
    
    collection.push(newItem)
    this.cache[nextIdKey]++
    this.save()
    
    return newItem
  }

  update(collectionName, id, updates) {
    const collection = this.getCollection(collectionName)
    const index = collection.findIndex(item => item.id === id)
    
    if (index === -1) return null
    
    collection[index] = {
      ...collection[index],
      ...updates
    }
    
    this.save()
    return collection[index]
  }

  updateOne(collectionName, predicate, updates) {
    const collection = this.getCollection(collectionName)
    const index = collection.findIndex(item => {
      if (typeof predicate === 'function') {
        return predicate(item)
      }
      for (const key in predicate) {
        if (item[key] !== predicate[key]) return false
      }
      return true
    })
    
    if (index === -1) return null
    
    collection[index] = {
      ...collection[index],
      ...updates
    }
    
    this.save()
    return collection[index]
  }

  updateMany(collectionName, predicate, updates) {
    const collection = this.getCollection(collectionName)
    let count = 0
    
    collection.forEach((item, index) => {
      let matches = false
      if (typeof predicate === 'function') {
        matches = predicate(item)
      } else {
        matches = true
        for (const key in predicate) {
          if (item[key] !== predicate[key]) {
            matches = false
            break
          }
        }
      }
      
      if (matches) {
        collection[index] = {
          ...collection[index],
          ...updates
        }
        count++
      }
    })
    
    if (count > 0) this.save()
    return count
  }

  delete(collectionName, id) {
    const collection = this.getCollection(collectionName)
    const index = collection.findIndex(item => item.id === id)
    
    if (index === -1) return false
    
    collection.splice(index, 1)
    this.save()
    return true
  }

  deleteOne(collectionName, predicate) {
    const collection = this.getCollection(collectionName)
    const index = collection.findIndex(item => {
      if (typeof predicate === 'function') {
        return predicate(item)
      }
      for (const key in predicate) {
        if (item[key] !== predicate[key]) return false
      }
      return true
    })
    
    if (index === -1) return false
    
    collection.splice(index, 1)
    this.save()
    return true
  }

  deleteMany(collectionName, predicate) {
    const collection = this.getCollection(collectionName)
    const initialLength = collection.length
    
    const filtered = collection.filter(item => {
      if (typeof predicate === 'function') {
        return !predicate(item)
      }
      for (const key in predicate) {
        if (item[key] === predicate[key]) return false
      }
      return true
    })
    
    this.cache[collectionName] = filtered
    this.save()
    
    return initialLength - filtered.length
  }

  count(collectionName, predicate) {
    const collection = this.getCollection(collectionName)
    if (!predicate) return collection.length
    
    return collection.filter(item => {
      if (typeof predicate === 'function') {
        return predicate(item)
      }
      for (const key in predicate) {
        if (item[key] !== predicate[key]) return false
      }
      return true
    }).length
  }

  set(key, value) {
    this.cache[key] = value
    this.save()
  }

  get(key) {
    return this.cache[key]
  }
}

const usersStore = new JsonStore('users.json')
const seatsStore = new JsonStore('seats.json')
const reservationsStore = new JsonStore('reservations.json')
const notificationsStore = new JsonStore('notifications.json')
const favoritesStore = new JsonStore('favorites.json')
const leisureRoomsStore = new JsonStore('leisure-rooms.json')

export {
  JsonStore,
  usersStore,
  seatsStore,
  reservationsStore,
  notificationsStore,
  favoritesStore,
  leisureRoomsStore
}
