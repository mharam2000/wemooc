package com.liferay.lms.asset;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import com.liferay.lms.learningactivity.LearningActivityType;
import com.liferay.lms.learningactivity.LearningActivityTypeRegistry;
import com.liferay.lms.model.Course;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.service.ClpSerializer;
import com.liferay.lms.service.CourseLocalServiceUtil;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.lms.service.LmsPrefsLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortletBagPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.portlet.asset.model.AssetRenderer;
import com.liferay.portlet.asset.model.BaseAssetRendererFactory;

public class LearningActivityAssetRendererFactory extends BaseAssetRendererFactory
 {
	public static final String CLASS_NAME = LearningActivity.class.getName();
	public static final String TYPE = "learningactivity";
	private static final String PORTLET_ID =  PortalUtil.getJsSafePortletId("lmsactivitieslist"+PortletConstants.WAR_SEPARATOR+ClpSerializer.getServletContextName());
	private volatile Method getGroupIds;
	
	public LearningActivityAssetRendererFactory() throws SystemException {
		try {
 			Class<?> assetPublisherUtilClass = PortalClassLoaderUtil.getClassLoader().loadClass("com.liferay.portlet.assetpublisher.util.AssetPublisherUtil");
 			getGroupIds = assetPublisherUtilClass.getMethod("getGroupIds", PortletPreferences.class, Long.TYPE, Layout.class);
		} catch (Throwable e) {
			throw new SystemException(e);
		}
	}
	
	public long[] getGroupIds(
			PortletPreferences portletPreferences, long scopeGroupId,
			Layout layout) throws SystemException{
		try {
			return (long[]) getGroupIds.invoke(null, portletPreferences, scopeGroupId, layout);
		} catch (Throwable e) {
			throw new SystemException(e);
		} 
		
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<Long, String> getClassTypes(long[] groupId, Locale locale)
			throws Exception {

		Set<Long> companies = new HashSet<Long>();
		Set<Long> activityTypes = new HashSet<Long>();
		Map<Long, String> classTypes = new HashMap<Long, String>();
		LearningActivityTypeRegistry learningActivityTypeRegistry = new LearningActivityTypeRegistry();
		ResourceBundle resourceBundle = PortletBagPool.get(getPortletId()).getResourceBundle(locale);
		
		long[] invisibleTypes = StringUtil.split(PropsUtil.get("lms.learningactivity.invisibles"), StringPool.COMMA, GetterUtil.DEFAULT_LONG);
		for (Course course : (List<Course>)CourseLocalServiceUtil.dynamicQuery(
				CourseLocalServiceUtil.dynamicQuery().
					add(PropertyFactoryUtil.forName("groupCreatedId").in(ArrayUtil.toArray(groupId))))) {
			if(companies.add(course.getCompanyId())){
				for(long typeId:
					StringUtil.split(LmsPrefsLocalServiceUtil.getLmsPrefsIni(course.getCompanyId()).getActivities(), 
						StringPool.COMMA, -1)){
					if((typeId>=0)
							&&(!ArrayUtil.contains(invisibleTypes, typeId))
							&&(activityTypes.add(typeId))) {
						String learningActivityTypeName = learningActivityTypeRegistry.getLearningActivityType(typeId).getName();
						classTypes.put(typeId, (resourceBundle.containsKey(learningActivityTypeName)?
								resourceBundle.getString(learningActivityTypeName):learningActivityTypeName));
					}
				}
			}
		}

		return classTypes;
	}

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}
	
	private long getPlid(long courseId,ThemeDisplay themeDisplay) throws SystemException{

		if((Validator.isNull(courseId))&&
				(themeDisplay.getLayout().getFriendlyURL().equals("/reto"))) {
			return themeDisplay.getLayout().getPlid();
		}
			
		Course course = null;

		if(Validator.isNotNull(courseId)) {
			course = CourseLocalServiceUtil.fetchCourse(courseId);
		}

		if(Validator.isNull(course)) {
			course = CourseLocalServiceUtil.fetchByGroupCreatedId(themeDisplay.getScopeGroupId());
		}

		if(Validator.isNull(course)) {
			return GetterUtil.DEFAULT_LONG;
		}

		@SuppressWarnings("unchecked")
		List<Layout> layouts = LayoutLocalServiceUtil.dynamicQuery(LayoutLocalServiceUtil.dynamicQuery().
				add(PropertyFactoryUtil.forName("privateLayout").eq(false)).
				add(PropertyFactoryUtil.forName("companyId").eq(course.getCompanyId())).
				add(PropertyFactoryUtil.forName("groupId").eq(course.getGroupCreatedId())).
				add(PropertyFactoryUtil.forName("friendlyURL").eq("/reto")),0,1);

		if(layouts.isEmpty()) {
			return GetterUtil.DEFAULT_LONG;
		}
		else {
			return layouts.get(0).getPlid();
		}	
	}

	public AssetRenderer getAssetRenderer(long classPK, int type)throws PortalException, SystemException {
		LearningActivity learningactivity = LearningActivityLocalServiceUtil.getLearningActivity(classPK);
		LearningActivityType learningActivityType=new LearningActivityTypeRegistry().getLearningActivityType(learningactivity.getTypeId());
		return learningActivityType.getAssetRenderer(learningactivity);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public PortletURL getURLAdd(LiferayPortletRequest liferayPortletRequest, LiferayPortletResponse liferayPortletResponse) {
		
		try {
			List<Course> courses = null;
			ThemeDisplay themeDisplay = (ThemeDisplay)liferayPortletRequest.getAttribute(WebKeys.THEME_DISPLAY);
			long type = GetterUtil.getLong(liferayPortletRequest.getAttribute(WebKeys.ASSET_RENDERER_FACTORY_CLASS_TYPE_ID));
			long courseId = GetterUtil.getLong(liferayPortletRequest.getAttribute("courseId"));


			if(Validator.isNull(courseId)) {
				courses = CourseLocalServiceUtil.dynamicQuery(
						CourseLocalServiceUtil.dynamicQuery().
							add(PropertyFactoryUtil.forName("groupCreatedId").
								in(ArrayUtil.toArray(
									getGroupIds(themeDisplay.getPortletDisplay().getPortletSetup(), 
												themeDisplay.getScopeGroupId(), themeDisplay.getLayout())))));
				
				List<Course> filterCourses = new ArrayList<Course>(courses.size());
				for (Course course : courses) {
					if(ArrayUtil.contains(StringUtil.split(LmsPrefsLocalServiceUtil.getLmsPrefsIni(course.getCompanyId()).getActivities(), 
							StringPool.COMMA, GetterUtil.DEFAULT_LONG), type)){
						filterCourses.add(course);
					}
				}
				if(!filterCourses.isEmpty()) {
					courseId = courses.get(0).getCourseId();
				}
				courses = filterCourses;
			}
			
			long plid = getPlid(courseId,themeDisplay);
			
			if(Validator.isNull(plid)) {
				return null;
			}			

			long resModuleId = GetterUtil.getLong(liferayPortletRequest.getAttribute("resModuleId"));
	  	  	PortletURL portletURL = PortletURLFactoryUtil.create(liferayPortletRequest,PORTLET_ID,plid,PortletRequest.RENDER_PHASE);

	  	  	if((courses!=null)&&
	  	  	   (courses.size()>1)) {
	  	  		portletURL.setParameter("mvcPath", "/html/lmsactivitieslist/selectCourse.jsp");
	  	  		portletURL.setParameter("assetRendererId",themeDisplay.getPortletDisplay().getId());
	  	  		portletURL.setParameter("assetRendererPlid",Long.toString(themeDisplay.getPlid()));
	  	  		if(Validator.isNull(type)){
	  	  			portletURL.setParameter("type", Long.toString(type));
	  	  		}
	  	  	}
	  	  	else if(Validator.isNull(type)) {
				portletURL.setParameter("mvcPath", "/html/lmsactivitieslist/newactivity.jsp");
			}
			else {
				portletURL.setParameter("mvcPath", "/html/editactivity/editactivity.jsp");
				portletURL.setParameter("type", Long.toString(type));
			}

			if(Validator.isNotNull(resModuleId)) {
				portletURL.setParameter("resModuleId", Long.toString(resModuleId));
			}

			return portletURL;
		}
		catch(Throwable t) {
			return null;
		}
    }
	
	@Override
	public boolean hasPermission(PermissionChecker permissionChecker,
			long classPK, String actionId) throws Exception {
		LearningActivity learningActivity = 
				LearningActivityLocalServiceUtil.getLearningActivity(classPK);
		return permissionChecker.
				hasPermission(learningActivity.getGroupId(),LearningActivity.class.getName(),classPK,actionId);

	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isSelectable() {
		return true;
	}
	@Override
	public boolean isLinkable() {
		return true;
	}
	



}
