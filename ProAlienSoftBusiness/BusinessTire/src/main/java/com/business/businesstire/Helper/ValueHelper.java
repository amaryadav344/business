package com.business.businesstire.Helper;

import com.business.businessobjects.BusBase;
import com.business.businessobjects.DoBase;
import com.business.businesstire.Cache.MetaDataCache;
import com.business.utils.models.Entity.ICollection;
import com.business.utils.models.Entity.IColumn;
import com.business.utils.models.Entity.IEntity;
import com.business.utils.models.Entity.IProperty;
import com.business.utils.models.UI.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ValueHelper {
    public static JSONObject GetFormData(IEntity entity, IForm form, BusBase busBase) {
        IView control = form.getControl();
        JSONObject jsonObject = new JSONObject();
        GetDataForControl(entity, control, busBase, jsonObject);
        return jsonObject;
    }

    private static void GetDataForControl(IEntity entity, IView control, BusBase busBase, JSONObject jsonObject) {
        if (control instanceof IListView) {
            IListView listView = (IListView) control;
            if (entity.getCollections() != null) {
                Arrays.stream(entity.getCollections())
                        .filter(collection -> collection.getName()
                                .equals(listView.getEntityField())).findFirst()
                        .ifPresent(collection -> GetListViewData(collection, listView, busBase, jsonObject));
                ;
            }
        } else if (control instanceof IStackLayout) {
            if (((IStackLayout) control).getControls() != null) {
                IView[] controls = ((IStackLayout) control).getControls();
                for (IView view : controls) {
                    GetDataForControl(entity, view, busBase, jsonObject);
                }
            }
        } else if (control instanceof IGridLayout) {
            if (((IGridLayout) control).getControls() != null) {
                IView[] controls = ((IGridLayout) control).getControls();
                for (IView view : controls) {
                    GetDataForControl(entity, view, busBase, jsonObject);
                }
            }
        } else if (control instanceof IScrollView) {
            if (((IScrollView) control).getControl() != null) {
                GetDataForControl(entity, ((IScrollView) control).getControl(), busBase, jsonObject);
            }
        } else if (control instanceof IInput) {
            Object value = GetPropValue(((IInput) control).getEntityField(), entity, busBase);
            if (value != null) {
                jsonObject.put(control.getId(), value.toString());
            }
        } else if (control instanceof ILabel) {
            Object value = GetPropValue(((ILabel) control).getEntityField(), entity, busBase);
            if (value != null) {
                jsonObject.put(control.getId(), value.toString());
            }
        }
    }

    private static Object GetPropValue(String entityField, IEntity entity, BusBase busBase) {
        // Load Column Value
        if (entity.getColumns() != null) {
            Optional<IColumn> optionalIColumn = Arrays.stream(entity.getColumns()).filter(x -> x.getName().equals(entityField)).findFirst();
            if (optionalIColumn.isPresent()) {
                IColumn column = optionalIColumn.get();
                try {
                    DoBase doBase = (DoBase) busBase.getClass().getDeclaredField(entity.getDatabaseObjectField()).get(busBase);
                    return GetColumnValue(column, doBase);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
        // Load Property Value
        if (entity.getProperties() != null) {
            Optional<IProperty> optionalIProperty = Arrays.stream(entity.getProperties())
                    .filter(x -> x.getName().equals(entityField))
                    .findFirst();
            if (optionalIProperty.isPresent()) {
                IProperty property = optionalIProperty.get();
                return getObject(busBase, property.getObjectField());
            }

        }
        // Load Collection Value
        if (entity.getCollections() != null) {
            Optional<ICollection> optionalICollection = Arrays.stream(entity.getCollections())
                    .filter(x -> x.getName().equals(entityField))
                    .findFirst();
            if (optionalICollection.isPresent()) {
                ICollection collection = optionalICollection.get();
                return getObject(busBase, collection.getObjectField());
            }

        }

        return null;
    }

    private static Object getObject(BusBase busBase, String objectField) {
        String[] QualifiedPath = objectField.split(".//");
        Object value = busBase;
        for (String symbol : QualifiedPath) {
            try {
                if (value != null) {
                    value = value.getClass().getDeclaredField(symbol).get(busBase);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                value = null;
            }
        }
        return value;
    }

    private static void GetListViewData(ICollection collection, IListView listView, BusBase busBase, JSONObject jsonObject) {
        JSONArray jsonArray = new JSONArray();
        String[] QualifiedSymbols = collection.getObjectField().split("\\.");
        Object Collection = null;
        for (String symbol : QualifiedSymbols) {
            try {
                Collection = busBase.getClass().getDeclaredField(symbol).get(busBase);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                Collection = null;
            }
        }
        if (Collection != null) {
            if (Collection instanceof List) {
                List<BusBase> busBases = (List<BusBase>) Collection;
                IEntity entity = (IEntity) MetaDataCache.getInstance().getXmlCache().Get(collection.getEntity());
                for (int i = 0; i < busBases.size(); i++) {
                    JSONObject jsonObject1 = new JSONObject();
                    GetDataForControl(entity, listView.getControl(), busBases.get(i), jsonObject1);
                    jsonObject1.put("index", i);
                    jsonArray.put(jsonObject1);
                }

            }
        }
        jsonObject.put(listView.getId(), jsonArray);
    }

    public static Object GetColumnValue(IColumn iColumn, DoBase doBase) throws NoSuchFieldException, IllegalAccessException {
        return doBase.getClass().getField(iColumn.getObjectField()).get(doBase);
    }

/*


    public static JSONObject GetObjectValue(IObject iObject, BusBase busBase) throws NoSuchFieldException, IllegalAccessException {
        String[] spit = iObject.getObjectField().split("\\.");
        for (String prop : spit) {
            Object fvalue = busBase.getClass().getDeclaredField(prop).get(busBase);
            if (fvalue instanceof BusBase) {
                busBase = (BusBase) fvalue;
            }

        }
        IEntity entity = (IEntity) MetaDataCache.getInstance().getXmlCache().Get(iObject.getEntity());
        return GetFormData(entity, busBase);
    }

    public static JSONArray GetCollectionValue(ICollection collection, BusBase busBase) throws NoSuchFieldException, IllegalAccessException {
        JSONArray jsonArray = new JSONArray();
        String[] spit = collection.getObjectField().split("\\.");
        JSONObject value = new JSONObject();
        List<BusBase> Collection = new ArrayList<>();
        for (String prop : spit) {
            Object fvalue = busBase.getClass().getDeclaredField(prop).get(busBase);
            if (fvalue instanceof BusBase) {
                busBase = (BusBase) fvalue;
            } else if (fvalue instanceof List) {
                Collection = (List<BusBase>) fvalue;
            }
        }
        IEntity entity = (IEntity) MetaDataCache.getInstance().getXmlCache().Get(collection.getEntity());
        for (BusBase busBase1 : Collection) {
            JSONObject object = GetFormData(entity, busBase1);
            jsonArray.put(object);
        }
        return jsonArray;
    }

    public static JSONObject GetFormData(IEntity entity, IForm form, BusBase busBase) {
        IView control = form.getControl();
        JSONObject jsonObject = GetDataFromControl(entity, control, busBase);


        *//*if (entity.getColumns() != null) {
            for (IColumn column : entity.getColumns()) {
                try {
                    DoBase doBase = (DoBase) busBase.getClass().getDeclaredField(entity.getDatabaseObjectField()).get(busBase);
                    if (doBase != null) {
                        jsonObject.put(column.getName(), GetColumnValue(column, doBase));
                    }

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (entity.getProperties() != null) {
            for (IProperty property : entity.getProperties()) {
                try {
                    jsonObject.put(property.getName(), GetPropertyValue(property, busBase));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (entity.getObjects() != null) {
            for (IObject object : entity.getObjects()) {
                try {
                    jsonObject.put(object.getName(), GetObjectValue(object, busBase));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }*//*
        return jsonObject;
    }

    private static JSONObject GetDataFromControl(IEntity entity, IView control, BusBase busBase) {
        DoBase doBase = null;
        try {
            doBase = (DoBase) busBase.getClass().getDeclaredField(entity.getDatabaseObjectField()).get(busBase);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        if (control instanceof IListView) {
            IListView listView = (IListView) control;
            if (entity.getCollections() != null) {
                Optional<ICollection> optionalICollection = Arrays.stream(entity.getCollections()).filter(iCollection -> iCollection.getName().equals(listView.getEntityField())).findFirst();
                if (optionalICollection.isPresent()) {
                    ICollection collection = optionalICollection.get();
                    IEntity entity1 = (IEntity) MetaDataCache.getInstance().getXmlCache().Get(collection.getEntity());
                }

            }

        }

        return jsonObject;
    }

    public Object GetAttributeValue(String name, BusBase busBase) throws NoSuchFieldException, IllegalAccessException {
        String[] spit = name.split("\\.");
        Object value = new Object();
        for (String prop : spit) {
            Object fvalue = busBase.getClass().getDeclaredField(prop).get(busBase);
            if (fvalue instanceof BusBase) {
                busBase = (BusBase) fvalue;
            } else {
                value = fvalue;
            }
        }
        return value;
    }*/


}
